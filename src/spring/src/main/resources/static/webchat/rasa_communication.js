const socket = new WebSocket('ws://localhost:11001');
const messages = document.getElementById('messages');


// -----------------------------------------------------
const utterThis = new SpeechSynthesisUtterance()
const synth = window.speechSynthesis
let ourText = ""


var sendForm = document.querySelector('#chatform'),
  textInput = document.querySelector('.chatbox'),
  chatList = document.querySelector('.chatlist'),
  userBubble = document.querySelectorAll('.userInput'),
  botBubble = document.querySelectorAll('.bot__output'),
  animateBotBubble = document.querySelectorAll('.bot__input--animation'),
  overview = document.querySelector('.chatbot__overview'),
  hasCorrectInput,
  imgLoader = false,
  animationCounter = 1,
  animationBubbleDelay = 600,
  input,
  previousInput,
  isReaction = false,
  unkwnCommReaction = "I didn't quite get that.",
  chatbotButton = document.querySelector(".submit-button")

  var currentUrl = window.location.href;
  var curr_url = currentUrl.split('#')[0];
  //get functions directly from url
  if (currentUrl.split('#').length > 1)
  {
    var sessionID = currentUrl.split('#')[1];
    socket.addEventListener('open', (event) => {
    passSession(sessionID);
    getDiscourse(sessionID);
    });
   }
  else
  {
    console.log("new session");
    var sessionID = generateSessionID();
       socket.addEventListener('open', (event) => {
        passSession(sessionID);
        });
  }
  var newUrl = curr_url + "#" + sessionID;
  // Redirect to the new URL
  window.location.href = newUrl;


// send message to rasa on pressing Enter
sendForm.onkeydown = function (e) {
  if (e.keyCode == 13) {
    e.preventDefault();

    var input = textInput.value.toLowerCase();
    textInput.value = '';
    if (input.length > 0) {
      createBubble(input,"")
      sendMessage(input)
    }
  }
};

// Event listener for when a message is received from the backend
socket.addEventListener('message', (event) => {
  discourse_data = event.data.split('\n')

  for (let i = 0; i<discourse_data.length; i = i + 1)
    {
    if (discourse_data[i].includes('\x03') || discourse_data[i] == '') //for handling the unknown bits that are sent after connection is abruptly closed by browser
    {
    }
        else
        {
        console.log(discourse_data[i].split(":")[0]);


        if (discourse_data[i].split(":")[0] == "user")
            {

            createBubble(discourse_data[i].split(":")[1], "");
            }
        else
            {
            createBubble(discourse_data[i].split(":")[1], "green");
            }
        }
    }
   }
);



sendForm.addEventListener('submit', function (e) {
  //so form doesnt submit page (no page refresh)
  e.preventDefault();

  //No mix ups with upper and lowercases
  var input = textInput.value.toLowerCase();
  textInput.value = '';
  //Empty textarea fix
  if (input.length > 0) {
    createBubble(input, "")
    sendMessage(input)
  }
}) //end of eventlistener

var createBubble = function (input, color) {

  //create input bubble
  var chatBubble = document.createElement('li');
  if (color == ""){
  chatBubble.classList.add('userInput');
}
  else{
  chatBubble.classList.add('bot__output');
  }
  //adds input of textarea to chatbubble list item
  chatBubble.innerHTML = input;

  //adds chatBubble to chatlist
  chatList.appendChild(chatBubble)

}


function generateUUID() {
  let uuid = "", i, random;
  for (i = 0; i < 32; i++) {
    random = Math.random() * 16 | 0;
    if (i === 8 || i === 12 || i === 16 || i === 20) {
      uuid += "-";
    }
    uuid += (i === 12 ? 4 : (i === 16 ? (random & 3 | 8) : random)).toString(16);
  }
  return uuid;
}

function generateSessionID() {
  var sessionID = generateUUID()
  return sessionID;
}

//sending message to the server
function sendMessage(message) {
  if (socket && socket.readyState === socket.OPEN) {
    socket.send(message);
  } else {
    console.log('socket is not open');
  }
}

function getDiscourse(){
    var params = new URLSearchParams(window.location.search);
    var session = params.get('session');
    message = "getDiscourse";
    if (socket && socket.readyState === socket.OPEN) {
        socket.send(message);
      } else {
        console.log('socket is not open');
      }
}

function passSession(sessionID){
    if (socket && socket.readyState === socket.OPEN) {
        socket.send(sessionID);
      } else {
        console.log('socket is not open');
      }

}



