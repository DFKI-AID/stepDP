const ttsApp = new Vue({
    el: '#tts',
    data: {
        interval: null,
        outputHistory: [],
        voiceSelect: null,
        synth: null,
        voices: {},
        voiceStr: "",
        utterance: "hello world",
        manual_utterance: "",
        rate: 1,
        pitch: 1,
        ttsEnabled: true,
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
                this.utterance = this.outputHistory[this.outputHistory.length - 1];
                if (updated && this.ttsEnabled) {
                    this.stop();
                    this.tts(this.utterance);
                }
            }.bind(this));
            if ($.isEmptyObject(this.voices)) {
                //fix for chrome? somehow the voices can't be loaded during vue-create. (empty)
                //hence we reload them here if they are still empty
                this.populateVoiceList();
            }
        },
        tts: function (utterance) {
            var utterObj = new SpeechSynthesisUtterance(utterance);
            // var selectedOption = this.voice.getAttribute('data-name');
            utterObj.voice = this.voice;
            utterObj.pitch = this.pitch;
            utterObj.rate = this.rate;
            this.synth.speak(utterObj);

            // inputTxt.blur();
        },
        populateVoiceList() {
            this.voices = {};
            this.synth.getVoices().forEach(v => {
                let id = v.name + "-" + v.lang;
                this.voices[id] = v;
            });

        },
        play: function () {
            this.tts(this.manual_utterance);
        },
        stop: function () {
            this.synth.cancel();
        }
    },
    watch: {
        ttsEnabled: function (val, oldVal) {
            localStorage.setItem("ttsEnabled", val);
        }
    },
    created() {
        this.synth = window.speechSynthesis;
        this.ttsEnabled = JSON.parse(localStorage.getItem("ttsEnabled")) === true;
        this.updateData();
        this.interval = setInterval(function () {
            this.updateData();
        }.bind(this), 250);
        this.populateVoiceList();
    },
    computed: {
        voice: function () {
            return this.voices[this.voiceStr];
        }
    }
});