

var initAsr = function(grammar) {
    console.log("initializing asr");
    var SpeechRecognition = SpeechRecognition || webkitSpeechRecognition
    var SpeechGrammarList = SpeechGrammarList || webkitSpeechGrammarList
    var SpeechRecognitionEvent = SpeechRecognitionEvent || webkitSpeechRecognitionEvent

    // var colors = [ 'aqua' , 'azure' , 'beige', 'bisque', 'black', 'blue', 'brown', 'chocolate', 'coral', 'crimson', 'cyan', 'fuchsia', 'ghostwhite', 'gold', 'goldenrod', 'gray', 'green', 'indigo', 'ivory', 'khaki', 'lavender', 'lime', 'linen', 'magenta', 'maroon', 'moccasin', 'navy', 'olive', 'orange', 'orchid', 'peru', 'pink', 'plum', 'purple', 'red', 'salmon', 'sienna', 'silver', 'snow', 'tan', 'teal', 'thistle', 'tomato', 'turquoise', 'violet', 'white', 'yellow'];
    // var grammar.jsgf = '#JSGF V1.0; grammar.jsgf colors; public <color> = ' + colors.join(' | ') + ' ;'
    // let grammar.jsgf = ''

    var recognition = new SpeechRecognition();
    var speechRecognitionList = new SpeechGrammarList();
    speechRecognitionList.addFromString(grammar, 1);
    recognition.grammars = speechRecognitionList;
    //recognition.continuous = false;
    recognition.lang = 'en-US';
    recognition.interimResults = false;
    recognition.maxAlternatives = 1;

    var diagnostic = document.querySelector('.output');
    var bg = document.querySelector('html');
    var hints = document.querySelector('.hints');

    // hints.innerHTML = 'Tap/click then say a color to change the background color of the app. Try '+ colorHTML + '.';

    document.body.onclick = function () {
        recognition.start();
        console.log('Ready to receive a color command.');
    }

    recognition.onresult = function (event) {
        // The SpeechRecognitionEvent results property returns a SpeechRecognitionResultList object
        // The SpeechRecognitionResultList object contains SpeechRecognitionResult objects.
        // It has a getter so it can be accessed like an array
        // The [last] returns the SpeechRecognitionResult at the last position.
        // Each SpeechRecognitionResult object contains SpeechRecognitionAlternative objects that contain individual results.
        // These also have getters so they can be accessed like arrays.
        // The [0] returns the SpeechRecognitionAlternative at position 0.
        // We then return the transcript property of the SpeechRecognitionAlternative object

        console.log(JSON.stringify(event));

        var last = event.results.length - 1;
        var text = event.results[last][0].transcript;

        diagnostic.textContent = 'Result received: ' + text + '.';
        // bg.style.backgroundColor = color;
        console.log('Confidence: ' + event.results[0][0].confidence);
    }

    recognition.onspeechend = function () {
        recognition.stop();
    }

    recognition.onnomatch = function (event) {
        diagnostic.textContent = "I didn't recognise that color.";
    }

    recognition.onerror = function (event) {
        diagnostic.textContent = 'Error occurred in recognition: ' + event.error;
    }
}

$.get("/grammar.srgs", null, function(data) {
    console.log("retrieved grammar.srgs: " + data);
    initAsr(data);
}, "text");