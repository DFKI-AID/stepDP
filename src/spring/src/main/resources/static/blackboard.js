const blackboardOverviewApp = new Vue({
    el: '#blackboard-view',
    data: {
        interval: null,
        tokens: []
    },
    methods: {
        updateData: function () {
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
		tokenContentSelection: "",
        examples: {
            "add greeting": {"type": "GreetingIntent"},
			"add hello": {"type": "HelloIntent"},
        },
    },
    watch: {
        tokenContentSelection: function (val, oldVal) {
            let msg = this.examples[val];
            this.tokenContent = JSON.stringify(msg, null, 2);
        }
    },
    methods: {
        sendToken: function (event) {
            // var intent = JSON.loadStateChart(this.intentSelection);
            var intent = JSON.parse($("#tokenContentTextArea").val());
            this._send(intent, '/blackboard/addToken');
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
        }
    }
});