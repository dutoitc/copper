
class DataManager {
    constructor() {
        this.widgets=[];
        this.style=[];
        this.copperValues={};
        this.editable = false;
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
                    dataManager.import(json);
                } catch (e) {
                    alert("Erreur: " + e);
                }
            });
            return;
        }

        // Import
        this.widgets=json["widgets"];
        this.style=json["css"];
        if (this.style==null) this.style=[];
        this.refreshUI();
    }

    handleMessage(msg) {
        var spl = msg.split("/");

        // Time and reschedule
        if (spl[0]=="updateTime") {
            var now = new Date();
            var stime=now.getHours() + ":" + (now.getMinutes()<10?"0":"") + now.getMinutes() + ":" + (now.getSeconds()<10?"0":"") + now.getSeconds() ;
            $("#"+spl[1]).text(stime);
            setTimeout(function() { dataManager.handleMessage(msg); }, 1000);
        }

        // Date and reschedule
        if (spl[0]=="updateDate") {
            var now = new Date();
            var sdate=(now.getDate()<10?"0":"") + now.getDate() + "." + (now.getMonth()<9?"0":"") + (now.getMonth()+1) + "." + now.getFullYear();
            $("#"+spl[1]).text(sdate);
            setTimeout(function() { dataManager.handleMessage(msg); }, 1000);
        }

        // TODO: Websocket
        if (spl[0]=="refresh") {
            $.ajax({
                url: "http://localhost:30400/ws/values"
            }).done(function(data) {
                dataManager.copperValues = JSON.parse(data);
                if (!this.editable) {
                    dataManager.refreshUI();
                }
            });
        }

    }

    refreshUI() {
        // Build dom in a disconnected div
        var content = $("<div></div>");


        // Widgets
        for (var i=0; i<this.widgets.length; i++) {
            var widget = this.widgets[i];
            var ui = this.editable?new UIWidgetEditable(widget):new UIWidgetRunnable(widget);
            content.append(ui.buildDOM(this.copperValues));
        }

        // Style
        var style = "<style type='text/css'>";
        for (var i=0; i<this.style.length; i++) {
            style+=this.style[i]+"\n";
        }
        style+="</style>\n";
        content.append($(style));

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
