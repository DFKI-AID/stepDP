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
		tokenContentSelection: "",
        examples: {
            "add greeting": {"type": "GreetingIntent", "userName":"Alice"},
			"add hello": {"type": "HelloIntent"},
			"add bring intent with pizza": {
				  "type": "BringIntent",
				  "object": {
								"type" : "Pizza",
								"sort" : "Pizza Hawaii"
							},
				  "recipientName": "Alice"
				},
			"add bring intent with water": {
				  "type": "BringIntent",
				  "object": {
								"type" : "Water",
								"carbonated" : false
							},
				  "recipientName": "Alice"
				},
			"add bring intent": {
				  "type": "BringIntent",
				  "recipientName": "Alice"
				},
			"add pizza": {
				"type" : "Pizza",
				"sort" : "Hawaii"
			},
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