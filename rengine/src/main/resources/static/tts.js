const ttsApp = new Vue({
    el: '#tts',
    data: {
        interval: null,
        outputHistory: [],
        voiceSelect: null,
        synth: null,
        voices: null,
        voice: null,
        utterance: "hello world",
        rate: 1,
        pitch: 1,
    },
    computed: {
        getRowColor: function (event) {
            console.log(event);
        }
    },
    methods: {
        updateData: function () {
            $.get('/output/history', function (rsp) {
                let updated = this.outputHistory.length != rsp.length;
                this.outputHistory = rsp;
                this.utterance = this.outputHistory[this.outputHistory.length -1];
                if(updated) {
                    this.stop();
                    this.tts(this.utterance);
                }
            }.bind(this));
        },
        tts: function(utterance) {
            var utterObj = new SpeechSynthesisUtterance(utterance);
            // var selectedOption = this.voice.getAttribute('data-name');
            utterObj.voice = this.voice;
            utterObj.pitch = this.pitch;
            utterObj.rate = this.rate;
            this.synth.speak(utterObj);

            // inputTxt.blur();
        },
        populateVoiceList() {
            this.voices = this.synth.getVoices();
        },
        play: function() {
            this.tts(this.utterance);
        },
        stop: function() {
            this.synth.cancel();
        }
    },
    watch: {
        intentSelection: function (val, oldVal) {
            let msg = this.intents[val];
            this.intentContent = JSON.stringify(msg, null, 2);
        }
    },
    created() {
        this.synth = window.speechSynthesis;
        this.updateData();
        this.interval = setInterval(function () {
            this.updateData();
        }.bind(this), 250);
        this.populateVoiceList();
    }
});