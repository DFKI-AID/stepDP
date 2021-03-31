const blackboardOverviewApp = new Vue({
    el: '#blackboard-view',
    data: {
        interval: null,
        iteration: 0,
        tokens: []
    },
    methods: {
        updateData: function () {
            $.get('/iteration', function (response) {
                this.iteration = response.iteration;
            }.bind(this));

            $.get('/blackboard/active', function (response) {
                this.tokens = response;
            }.bind(this));
        }
    },    
    created() {
        this.updateData();
        this.interval = setInterval(function () {
            this.updateData();
        }.bind(this), 1000);

    }
});

const blackboardRuleApp = new Vue({
    el: '#blackboard-rules',
    data: {
        interval: null,
        rules: []
    },
    methods: {
        updateData: function () {
            $.get('/blackboard/rules', function (response) {
                this.rules = response;
            }.bind(this));
        }
    },    
    created() {
        this.updateData();
        this.interval = setInterval(function () {
            this.updateData();
        }.bind(this), 1000);

    }
});


const blackboardInputApp = new Vue({
    el: '#blackboard-input',
    data: {
		tokenContent: "",
		kbTokenContent: "",
		tokenContentSelection: "",
		kbTokenContentSelection: "",
        examples: {},
        kbExamples:  {
            "reference by uuid": {"uuid": "abcd1234-a1b2-a1b2-a1b2-ab12cd34ef56"},
            "reference by name": {"name": "bottle1"},
            "add tv (uuid)": {"uuid": "insert-uuid-of-a-tv-instance-here"},
            "add tv (name)": {"name": "tv1"}
        },
    },
    watch: {
        tokenContentSelection: function (val, oldVal) {
            let msg = this.examples[val];
            this.tokenContent = JSON.stringify(JSON.parse(msg), null, 2);
        },
        kbTokenContentSelection: function (val, oldVal) {
            let msg = this.kbExamples[val];
            this.kbTokenContent = JSON.stringify(JSON.parse(msg), null, 2);
        }
    },
    methods: {
        sendToken: function (event) {
            // var intent = JSON.loadStateChart(this.intentSelection);
            var intent = JSON.parse($("#tokenContentTextArea").val());
            this._send(intent, '/blackboard/addToken');
        },
        sendKBToken: function (event) {
            // var intent = JSON.loadStateChart(this.intentSelection);
            var intent = JSON.parse($("#kbTokenContentTextArea").val());
            this._send(intent, '/blackboard/addKBToken');
        },
        _send: function (jsonPayload, path) {
            console.log("sending " + jsonPayload + " to " + path);
            $.ajax({
                'url': path,
                'method': 'POST',
                'dataType': 'json',
                'contentType': 'application/json',
                'data': JSON.stringify(jsonPayload),
                'processData': false
            });
        },
        updateData: function () {
            $.get('/blackboard/exampleTokens', function (response) {
                this.examples = response;
            }.bind(this));
        }
    },
    created() {
        this.updateData();
    }
});