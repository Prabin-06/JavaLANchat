let username = "";
let lastMessageCount = 0;

// Connect to the chat
function connect() {
  username = document.getElementById("username").value.trim();

  if (username === "") {
    alert("Please enter a username");
    return;
  }

  document.getElementById("login").style.display = "none";
  document.getElementById("chat").style.display = "flex";

  addMessage("Connected as " + username);

  getMessages();
  setInterval(getMessages, 1000);
}

// Get messages from Java server
async function getMessages() {
  try {
    let response = await fetch(
      "http://" + location.hostname + ":8081/messages",
    );
    let data = await response.text();

    let messages = data ? data.split("\n") : [];

    for (let i = lastMessageCount; i < messages.length; i++) {
      if (messages[i] !== "") {
        addMessage(messages[i]);
      }
    }

    lastMessageCount = messages.length;
  } catch (error) {
    console.log("Unable to get messages");
  }
}

// Send a message
async function sendMessage() {
  let input = document.getElementById("messageInput");
  let message = input.value.trim();

  if (message === "") {
    return;
  }

  try {
    await fetch("http://" + location.hostname + ":8081/send", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body:
        "username=" +
        encodeURIComponent(username) +
        "&message=" +
        encodeURIComponent(message),
    });

    input.value = "";
  } catch (error) {
    alert("Unable to send message");
  }
}

// Display a message
function addMessage(message) {
  let messages = document.getElementById("messages");

  let item = document.createElement("p");
  item.textContent = message;

  messages.appendChild(item);

  messages.scrollTop = messages.scrollHeight;
}

// Send message using Enter key
document
  .getElementById("messageInput")
  .addEventListener("keydown", function (event) {
    if (event.key === "Enter") {
      sendMessage();
    }
  });
