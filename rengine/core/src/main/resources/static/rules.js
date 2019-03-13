const rulesApp = new Vue({
    el: '#rules',
    data: {
        interval: null,
        iteration: 0,
        rules: [],
        tagToColorMap: {},
        // background colors for the tags. add more if you run out of colors. otherwise they will repeat
        colors: ["black", "CornflowerBlue", "DarkSeaGreen", "DarkSalmon",
            "IndianRed", "SlateGray", "LightSeaGreen", "PaleVioletRed", "Indigo", "Teal"],
        colorIndex: 0,
    },
    computed: {
        getRowColor: function (event) {
            console.log(event);
        }
    },
    methods: {
        updateData: function () {
            $.get('/iteration', function (response) {
                this.iteration = response.iteration;
            }.bind(this));

            $.get('/rules', function (response) {
                this.rules = response;
            }.bind(this));
        }
    },
    watch: {
        intentSelection: function (val, oldVal) {
            let msg = this.intents[val];
            this.intentContent = JSON.stringify(msg, null, 2);
        }
    },
    created() {
        this.updateData();
        this.interval = setInterval(function () {
            this.updateData();
        }.bind(this), 250);

    },
    directives: {
        color: {
            update: function (el) {
                //TODO how do I get the this context here? instead of app
                var tag = $(el).text();
                var data = rulesApp.$data;
                var color = data.tagToColorMap[tag];
                // console.log(color);
                if (color == null) {
                    console.log("creating color for tag " + tag);
                    color = data.colors[data.colorIndex];
                    data.colorIndex += 1;
                    if (data.colorIndex >= data.colors.length) {
                        console.log("resetting color index");
                        data.colorIndex = 0;
                    }
                    data.tagToColorMap[tag] = color;
                }

                $(el).css({"background-color": color});
            }
        }
    }
});