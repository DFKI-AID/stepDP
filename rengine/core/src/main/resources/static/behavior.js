//deps:
// <script type="text/javascript">
// mxBasePath = '/external/mxgraph';
// </script>
// <script src="/external/mxgraph/js/mxClient.js"></script>

// <div ref="graphContainer"
// style="position:relative;overflow:hidden;width:5000px;height:5000px;background-color: #FDFFFC;cursor:default;">
//     </div>

const behaviorApp = new Vue({
    el: '#behavior',
    data: {
        interval: null,
        statechart: null,
        graph: null,
        currentState: null,
    },
    methods: {
        updateSC() {
            $.get('/behavior/task_behavior', function (response) {
                this.statechart = response;
                var container = this.$refs.graphContainer;
                this.drawStateChart(container, this.statechart.root);
            }.bind(this));
        },

        updateSCState: function () {
            $.ajax({
                type: "GET",
                url: "/behavior/task_behavior/state",
                contentType: "application/json",
                dataType: "json",
                success: function (response) {
                    let state = response.state;
                    // if (state == this.currentState) {
                    //     return;
                    // }
                    // this.unhighlightState(this.currentState);
                    this.currentState = state;
                    // this.highlightState(this.currentState);
                    this.updateStyle();
                }.bind(this),
                error: function (x) {
                    console.warn("could not retrieve behavior state: " + x);
                }
            });

            // $.get('/behavior/task_behavior/state', function (response) {
            //     console.log("!!UWUWHW");
            // let state = response.state;
            // this.unhighlightState(this.currentState);
            // this.currentState = state;
            // this.highlightState(this.currentState);
            // }.bind(this));
        },
        drawStateChart: function (container, statechart) {
            // Checks if the browser is supported
            if (!mxClient.isBrowserSupported()) {
                // Displays an error message if the browser is not supported.
                mxUtils.error('Browser is not supported!', 200, false);
            } else {
                // Disables the built-in context menu
                mxEvent.disableContextMenu(container);

                // Creates the graph inside the given container
                this.graph = new mxGraph(container);
                this.graph.getView().setScale(0.8);


                // Create cell styles
                let defaultLeafStyle = new Object();
                defaultLeafStyle[mxConstants.STYLE_FONTCOLOR] = 'white';
                defaultLeafStyle[mxConstants.STYLE_FILLCOLOR] = '#23395B';
                defaultLeafStyle[mxConstants.STYLE_FONTFAMILY] = 'Open Sans';
                defaultLeafStyle[mxConstants.STYLE_FONTSIZE] = '13';
                defaultLeafStyle[mxConstants.STYLE_STROKECOLOR] = "black";
                this.graph.getStylesheet().putCellStyle("DefaultLeaf", defaultLeafStyle);

                let defaultNodeStyle = JSON.parse(JSON.stringify(defaultLeafStyle));
                defaultNodeStyle[mxConstants.STYLE_FILLCOLOR] = '#406E8E';
                this.graph.getStylesheet().putCellStyle("DefaultNode", defaultNodeStyle);

                let highlightStyle = JSON.parse(JSON.stringify(defaultLeafStyle));
                highlightStyle[mxConstants.STYLE_FILLCOLOR] = '#CBF7ED';
                highlightStyle[mxConstants.STYLE_FONTCOLOR] = 'black';
                defaultLeafStyle[mxConstants.STYLE_STROKECOLOR] = "#23395B";
                this.graph.getStylesheet().putCellStyle("Active", highlightStyle);


                // Enables rubberband selection
                new mxRubberband(this.graph);

                // Gets the default parent for inserting new cells. This
                // is normally the first child of the root (ie. layer 0).
                this.parent = this.graph.getDefaultParent();

                var context = {};
                context.graph = this.graph;
                context.bbox = {minx: 0, miny: 0, maxx: 0, maxy: 0};
                this.getBoundingBox(context, statechart);
                console.log(context.bbox);
                context.vertices = {};


                // Adds cells to the model in a single step
                this.graph.getModel().beginUpdate();
                try {
                    this.drawState(context, this.parent, statechart);
                    this.drawTransitions(context, this.parent, statechart);
                    // var v1 = graph.insertVertex(parent, null, 'Hello,', 20, 20, 80, 30);
                    // var v2 = graph.insertVertex(parent, null, 'World!', 200, 150, 80, 30);
                    // var e1 = graph.insertEdge(parent, null, '', v1, v2);
                } finally {
                    // Updates the display
                    this.graph.getModel().endUpdate();
                }

                this.updateStyle();

                // var encoder = new mxCodec();
                // var node = encoder.encode(v1);
                // console.log(mxUtils.getXml(node));
            }
        },

        updateStyle: function () {
            if (this.parent == null) {
                return;
            }

            this.apply(this.parent, (state) => {

                if (state.children != null && state.children.length > 0) {
                    //intermediate node
                    state.setStyle("DefaultNode");
                } else {
                    //leaf node
                    if (this.currentState != null && this.currentState == state.id) {
                        state.setStyle("Active");
                    } else {
                        state.setStyle("DefaultLeaf");
                    }
                }

                // refresh cell in graph
                this.graph.getView().clear(state, false, false);
                this.graph.getView().validate();
            });
        },

        highlightState: function (state) {
            if (state == null || this.graph == null) {
                return;
            }
            let stateCell = this.graph.getModel().getCell(state);
            if (stateCell == null) {
                console.warn("can't highlight cell: No cell found with id " + state);
                return;
            }

            stateCell.setStyle("Active");
            // refresh cell in graph
            this.graph.getView().clear(stateCell, false, false);
            this.graph.getView().validate();
        },

        unhighlightState: function (state) {
            if (state == null || this.graph == null) {
                return;
            }
            let stateCell = this.graph.getModel().getCell(state);
            if (stateCell == null) {
                console.warn("can't highlight cell: No cell found with id " + state);
                return;
            }
            stateCell.setStyle("Default");
            // refresh cell in graph
            this.graph.getView().clear(stateCell, false, false);
            this.graph.getView().validate();
        },

        apply: function (state, fnc) {
            if (state == null) {
                return;
            }

            fnc(state);
            if (state.children == null) {
                return;
            }
            for (let i = 0; i < state.children.length; ++i) {
                let childState = state.children[i];
                this.apply(childState, fnc);
            }
        },

        drawState: function (context, parent, state) {
            var geo = state.geometry;
            if (geo == null) {
                geo = {x: 0, y: 0, w: 50, h: 50};
            }
            var vertex = context.graph.insertVertex(parent, state.id, state.id,
                geo.x - context.bbox.minx,
                geo.y - context.bbox.miny,
                geo.w,
                geo.h
            );
            context.vertices[state.id] = vertex;

            for (var i = 0; i < state.children.length; ++i) {
                var childState = state.children[i];
                this.drawState(context, vertex, childState);
            }
        },

        drawTransitions: function (context, parent, state) {

            for (var i = 0; i < state.transitions.length; ++i) {
                var transition = state.transitions[i];
                var v1 = context.vertices[state.id];
                var v2 = context.vertices[transition.target];
                var e1 = context.graph.insertEdge(parent, null, '', v1, v2);
            }

            for (var i = 0; i < state.children.length; ++i) {
                var childState = state.children[i];
                this.drawTransitions(context, parent, childState);
            }
        },

        getBoundingBox: function (context, state) {
            var geo = state.geometry;
            if (geo != null) {
                context.bbox.minx = Math.min(context.bbox.minx, geo.x);
                context.bbox.miny = Math.min(context.bbox.miny, geo.y);
                context.bbox.maxx = Math.max(context.bbox.minx, geo.x + geo.w);
                context.bbox.maxy = Math.max(context.bbox.miny, geo.y + geo.h);
            }
            for (var i = 0; i < state.children.length; ++i) {
                var childState = state.children[i];
                this.getBoundingBox(context, childState);
            }
        }

    },
    created() {
        this.updateSC();
        this.updateSCState();
        this.interval = setInterval(function () {
            this.updateSCState();
        }.bind(this), 1000);

    }

});