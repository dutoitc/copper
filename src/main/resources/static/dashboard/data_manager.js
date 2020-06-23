
class DataManager {
    constructor() {
        this.widgets=[];
        this.style=[];
        this.script=[];
        this.copperValues={};
        this.copperStatus="KO loading";
        this.editable = false;
        this.skipRefresh = false;
        this.jsonScreens = [];

        // Load JSON from local by webservice
        $.ajax({
            url: "../ws/screens"
            //  Format: [ {'name':'screen1', 'data':jsonScreen}, ... ]
        }).done(function(data) {
            var jsonScreens = data;
            dataManager.jsonScreens = data;
            var nb = Object.getOwnPropertyNames(jsonScreens).length;
            if (nb==0) {
                // Load JSON from local storage
                var json = localStorage.getItem("copperJson");
                if (json!=null && confirm("Use last dashboard ?")) dataManager.importJSON(JSON.parse(json));
            } else if (nb==1) {
                dataManager.defineScreenJsonObject(jsonScreens[0].data);
            } else if (nb>1) {
                dataManager.chooseAndImportScreen(jsonScreens);
            }
        });
    }

    chooseAndImportScreen(jsonScreens) {
        var screens = jsonScreens;
        $( function() {
            var buttons = {};
            for (var screenName in screens) {
                buttons[screenName] = function(localScreenName) {
                    return function() {
                        dataManager.defineScreenJsonObject(JSON.parse(screens[localScreenName]));
                        $( this ).dialog( "close" );
                    }
                }(screenName)
            }

            $( "#dialog-screenchoice" ).visibility='visible';
            $( "#dialog-screenchoice" ).dialog({
              resizable: false,
              height: "auto",
              width: 400,
              modal: true,
              style: 'visibility: visible',
              buttons: buttons
              /*{
                "Ecran 1": function() {
                    dataManager.defineScreenJsonObject(JSON.parse(dataManager.jsonScreens['myScreen1']));
                    $( this ).dialog( "close" );
                },
                "Ecran 2": function() {
                    dataManager.defineScreenJsonObject(JSON.parse(dataManager.jsonScreens['myScreen2']));
                    $( this ).dialog( "close" );
                }*/
                //}
            });
          } );
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
                    if (content==null) return;
                    json = JSON.parse(content);
                    dataManager.defineScreenJsonObject(json);
                } catch (e) {
                    alert("Erreur: " + e);
                }
            });
            return;
        }
    }


    // Use the given screen and save to local storage
    defineScreenJsonObject(json) {
        dataManager.importJSON(json);

        // Save for later use
        localStorage.setItem("copperJson", JSON.stringify(json));
        console.log("Setting copperJson",json);
    }

    importJSON(json) {
        // Import
        console.log("Importing JSON", json);
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
                dataManager.copperStatus='OK';
                if (!this.editable) {
                    dataManager.refreshUI();
                }
            }).fail(function() {
                dataManager.copperStatus='KO';
                console.log("ERROR: Copper values /ws/values read failed.");
                if (!this.editable) {
                    dataManager.refreshUI(); // Refresh with old values but with copperStatus KO
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
        for (var j=0; j<this.widgets.length; j++) {
            var widget = this.widgets[j];
            var ui = this.editable?new UIWidgetEditable(widget):new UIWidgetRunnable(widget);
            content.append(ui.buildDOM(this.copperValues));
        }

        // Script
        var script = "<script type='text/javascript'>";
        for (var k=0; k<this.script.length; k++) {
            script+=this.script[k]+"\n";
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
