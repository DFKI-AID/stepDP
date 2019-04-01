const inputApp = new Vue({
    el: '#input',
    data: {
        interval: null,
        grammar: null,
        intents: {
            "select_task": {"intent": "select", "selection": "task2", "confidence": 0.8},
            "accept_task": {"intent": "accept_task", "confidence": 0.8},
            "specify_task": {"intent": "specify", "specification": "task3"},
            "show_tasks": {"intent": "show_tasks"},
            "hide_tasks": {"intent": "hide_tasks"},
            "confirm": {"intent": "answer", "content": "confirm"},
            "disconfirm": {"intent": "answer", "content": "disconfirm"},
            "hello": {"intent": "greetings"},
            "undo": {"intent": "undo"},
            "repeat": {"intent": "repeat"},
            "show_nav": {"intent": "request", "object": "navigation"},
            "hide_nav": {"intent": "hide_navigation"},
            "turn_grab": {"intent": "turn_grab"},
            "rewind": {"intent": "rewind", "name": "lunch_time"},
            "create_snapshot": {"intent": "create_snapshot", "name": "lunch_time"}
        },
        intentSelection: "",
        intentContent: "",
    },
    computed: {
        getRowColor: function (event) {
            console.log(event);
        }
    },
    methods: {
        updateData: function () {
            $.get('/grammar', function (rsp) {
                try {
                    this.grammar = new XMLSerializer().serializeToString(rsp);
                } catch(error) {
                    console.log("could not load grammar: " + error);
                    this.grammar = "";
                }
            }.bind(this));

        },
        sendIntent: function (event) {
            // var intent = JSON.loadStateChart(this.intentSelection);
            var intent = JSON.parse($("#intentTextArea").val());
            this._sendIntent(intent);
        },
        _sendIntent: function (intent) {
            console.log("sending intent " + intent);
            $.ajax({
                'url': '/input/intent',
                'method': 'POST',
                'dataType': 'json',
                'contentType': 'application/json',
                'data': JSON.stringify(intent),
                'processData': false
            });
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
        }.bind(this), 500);

    }
});