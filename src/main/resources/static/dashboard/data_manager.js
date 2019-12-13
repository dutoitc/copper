
class DataManager {
    constructor() {
        this.widgets=[];
        this.style=[];
        this.script=[];
        this.copperValues={};
        this.editable = false;
        this.skipRefresh = false;

        // Load from disk
        var json = localStorage.getItem("copperJson");
        if (json!=null && confirm("Use last dashboard ?")) this.importJSON(JSON.parse(json));
    }

    addWidget(x, y) {
        var widget = new Widget("w"+this.widgets.length, x, y);
        this.widgets.push(widget);
        this.refreshUI();
    }

    clear() {
        this.widgets=[];
        this.refreshUI();
    }

    toggleEditable() {
        this.editable = !this.editable;
        this.refreshUI();
    }

    toggleSkipRefresh() {
        this.skipRefresh = !this.skipRefresh;
        if (this.skipRefresh) {
            alert("Autorefresh is off");
        } else {
            alert("Autorefresh is on")
        }
    }

    /** Export to json file */
    export() {
        var objects={"widgets": this.widgets};
        var json = JSON.stringify(objects);
        downloadFile("application/json", "UTF-8", json, 'copper-ihm-export.json');
    }

    import(json) {
        if (json==null) {
            // Upload file, then call import with JSON
            uploadFile(function(content) {
                try {
                    //content = content.substr(content.indexOf(",")+1);
                    if (content==null) return;
                    json = JSON.parse(content);
                    dataManager.importJSON(json);

                    // Save for later use
                    localStorage.setItem("copperJson", JSON.stringify(json));
                    console.log("Setting copperJson",json);
                } catch (e) {
                    alert("Erreur: " + e);
                }
            });
            return;
        }
    }

    importJSON(json) {
        // Import
        this.widgets=json["widgets"];
        this.style=json["css"];
        this.script=json["script"];
        if (this.style==null) this.style=[];
        if (this.script==null) this.script=[];
        this.editable=false;
        this.handleMessage("refresh");
    }

    handleMessage(msg) {
        var spl = msg.split("/");
        var verb = spl[0];

        // Time and reschedule
        if (verb=="updateTime") {
            var now = new Date();
            var stime=now.getHours() + ":" + (now.getMinutes()<10?"0":"") + now.getMinutes() + ":" + (now.getSeconds()<10?"0":"") + now.getSeconds() ;
            $("#"+spl[1]).text(stime);
            setTimeout(function() { dataManager.handleMessage(msg); }, 1000);
        }

        // Date and reschedule
        if (verb=="updateDate") {
            var now = new Date();
            var sdate=(now.getDate()<10?"0":"") + now.getDate() + "." + (now.getMonth()<9?"0":"") + (now.getMonth()+1) + "." + now.getFullYear();
            $("#"+spl[1]).text(sdate);
            setTimeout(function() { dataManager.handleMessage(msg); }, 1000);
        }

        // TODO: Websocket
        if (verb=="refresh") {
            if (this.skipRefresh) return;
            $.ajax({
                url: "../ws/values"
            }).done(function(data) {
                dataManager.copperValues = JSON.parse(data);
                if (!this.editable) {
                    dataManager.refreshUI();
                }
            });
        }

    }

    // TODO: diff old and new dom, replace only new elements
    refreshUI() {
        // Build dom in a disconnected div
        var content = $("<div></div>");

        // Style
        var style = "<style type='text/css'>";
        for (var i=0; i<this.style.length; i++) {
            style+=this.style[i]+"\n";
        }
        style+="</style>\n";
        content.append($(style));

        // Widgets
        for (var i=0; i<this.widgets.length; i++) {
            var widget = this.widgets[i];
            var ui = this.editable?new UIWidgetEditable(widget):new UIWidgetRunnable(widget);
            content.append(ui.buildDOM(this.copperValues));
        }

        // Script
        var script = "<script type='text/javascript'>";
        for (var i=0; i<this.script.length; i++) {
            script+=this.script[i]+"\n";
        }
        script+="</script>\n";
        content.append($(script));

        // Replace data by built dom
        $("#data").empty();
        $("#data").append(content);

        // Toolbox
        if (this.editable) {
            $("#toolbox").draggable();
            $("#toolbox").show();
        } else {
            $("#toolbox").hide();
        }
    }



}
