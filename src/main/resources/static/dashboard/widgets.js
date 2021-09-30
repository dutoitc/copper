
// FIXME: jquery-ui/resize snap to grid is not working /(bug jquery?)


/* Data widget */
class Widget {
    constructor(id, x, y) {
        this.id=id;
        this.x=x;
        this.y=y;
        this.width=120;
        this.height=200;
        this.title="aWidget";
        this.body="Some body\nand a new line...";
        this.classes="";
    }
}

/* Widget mappable to DOM */
class UIWidget {

    constructor(widget) {
        this.widget = widget;
        this.widgetId = "obj" + this.widget.id;
        this.widgetTitleId = this.widgetId+"title";
        this.widgetBodyId = this.widgetId+"body";
    }

    buildDOM(copperValues) {
        //
        this.oDiv=  $("<div id='" + this.widgetId +      "' class='mybox ui-widget-content'></div>");
        //
        this.oDiv.css({"position": "absolute", "left":this.widget.x, "top":this.widget.y, "width":this.widget.width, "height":this.widget.height});
        return this.oDiv;
    }

}

class UIWidgetRunnable extends UIWidget {


    buildDOM(copperValues) {
        super.buildDOM(copperValues);
        var bodyHTML = this.widget.body;

        // #CSS_CLASS#
        var cssClasses="";
        while(bodyHTML.indexOf("#CSS_CLASS_")>-1) {
            var p = bodyHTML.indexOf("#CSS_CLASS_");
            var p2 = bodyHTML.indexOf("#", p+10);
            var cssClass = bodyHTML.substring(p+11, p2);
            bodyHTML = bodyHTML.substring(0,p) + bodyHTML.substring(p2+1);
            cssClasses+=cssClass+" ";
        }

        // #TIME#
        if (bodyHTML.indexOf("#TIME#")>-1) {
            var id = this.widgetBodyId+"-time";
            bodyHTML = bodyHTML.replace("#TIME#", "<span id='" + id + "'></span>");
            setTimeout("dataManager.handleMessage('updateTime/"+id+"');", 1000);
        }

        // #DATE#
        if (bodyHTML.indexOf("#DATE#")>-1) {
            var id2 = this.widgetBodyId+"-date";
            bodyHTML = bodyHTML.replace("#DATE#", "<span id='" + id2 + "'></span>");
            //setTimeout(function(id){ function() { dataManager.handleMessage("updateDate/" + id); }}(id), 1000);
            setTimeout("dataManager.handleMessage('updateDate/"+id2+"');", 1000);
        }

        // {{a_copper_value}} -> the_value
        while (bodyHTML.indexOf("{{")>-1) {
            var start = bodyHTML.indexOf("{{");
            var end = bodyHTML.indexOf("}}", start+2);
            if (end==-1) throw "Invalid HTML";
            var content = bodyHTML.substr(start+2, end-start-2);
            var content2 = this.evaluate(content, copperValues);
            bodyHTML = bodyHTML.substr(0, start) + content2 + bodyHTML.substr(end+2);
        }
        
        this.oTitle=$("<div id='" + this.widgetTitleId + "' class='ui-widget-header box-title'>" + this.widget.title + "</div>");
        this.oBody= $("<div id='" + this.widgetBodyId +  "' class='box-body'>" + bodyHTML + "</div>");
        this.oDiv.append(this.oTitle);
        this.oDiv.append(this.oBody);
        this.oDiv.addClass(cssClasses + " " + this.widget.classes);
        return this.oDiv;
    }

    evaluate(expression, copperValues) {
        'use strict'; // forbid modifying values with eval
        // API
        var cv = function(name) {
            if (copperValues[name]!=null) return copperValues[name];
            return {"id":-1,"key":"UNKNOWN","value":"?","timestampFrom":"1980-01-01T00:00:00","timestampTo":"3000-12-31T01:00:00","nbValues":1};
        }
        var widget = this.widget;
        var copperStatus = dataManager.copperStatus;

		try {
			return eval(expression);
		} catch(error) {
            console.log("Error", expression, error);
            return "<span style='color:red' title=\"Invalid expression: " + expression + ": " + error + "\">&#x26A0;</span>";
		}
    }
}



/** a Widget editable, UI components */
class UIWidgetEditable extends UIWidget {

    constructor(widget) {
        super(widget);
        this.widgetClassesId = this.widgetId+"class";
    }

    buildDOM(copperValues) {
        super.buildDOM(copperValues);
        this.oDiv.addClass("mybox_edit");
        this.oTitle=$("<div  id='" + this.widgetTitleId + "' class='ui-widget-header box-title'>" + this.widget.title + "</div>");
        this.oClasses= $("<div id='" + this.widgetClassesId +  "' style='height: 20px;background:#def;font-style:italic'>" + this.widget.classes + "</div>");
        this.oBody= $("<div id='" + this.widgetBodyId +  "' style='min-height: 20px;background:#fed' class='box-body'>" + this.widget.body + "</div>");
        this.oDiv.append(this.oTitle);
        this.oDiv.append(this.oClasses);
        this.oDiv.append(this.oBody);

        // Resize
        this.oDiv.resizable({
            /*grid: 10,*/
            helper: "ui-resizable-helper",
            stop: function(widget) {
                return function(evt, ui) {
                    widget.width = ui.size.width;
                    widget.height = ui.size.height;
                    dataManager.refreshUI();
                }
            }(this.widget)
        });

        // Draggable
        this.oDiv.draggable({
            /*grid: [10,10],*/
            stop: function(widget) {
                return function(evt, ui) {
                    widget.x = ui.position.left;
                    widget.y = ui.position.top;
                }
            }(this.widget)
        });

        // Edition of title
        this.oTitle.dblclick(function(widget) {
            return function() {
                var value = prompt("Edit title", widget.title);
                if (value!=null) {
                    widget.title = value;
                    dataManager.refreshUI();
                }
            }
        }(this.widget));

        // Edition of classes
        this.oClasses.dblclick(function(widget) {
            return function() {
                var value = prompt("Edit classes", widget.classes);
                if (value!=null) {
                    widget.classes = value;
                    dataManager.refreshUI();
                }
            }
        }(this.widget));

        // Edition of body
        this.oBody.dblclick(function(widget) {
            return function() {
                var value = prompt("Edit body", widget.body);
                if (value!=null) {
                    widget.body = value;
                    dataManager.refreshUI();
                }
            }
        }(this.widget));

        return this.oDiv;
    } // /buildDOM
}

