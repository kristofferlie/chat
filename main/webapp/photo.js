/**
 * Main controller class of forum. It will start its own background thread 
 * (worker.js) to read messages from the server.
 * 
 * @type type
 */
class PhotoForum {
   constructor() {
        this.photo = document.querySelector("#photo");
        this.forum = document.querySelector("#forum");
        this.message = document.querySelector("#message");
       
       
        this.name = new URL(document.URL).searchParams.get("name");
        this.loadImage(this.name);


        // Call login to force a BASIC authentication.
        // Why do I need to do a double call to get the JSESSIONID cookie???
        fetch('api/auth/login',{credentials: 'same-origin'})
            .then(response => {
                fetch('api/auth/login',{credentials: 'same-origin'}) 
                    .then(response => this.userid = response.json().userid);

        });

        this.message.onchange = event => this.addMessage(event.target.value);
       
        this.worker = new Worker("worker.js");
        this.worker.postMessage({"name" : this.name});
       
        this.worker.onmessage = event => {
           this.forum.innerHTML = '';
           let ul = document.createElement('ul');
           event.data.map(message => {
              let li = document.createElement('li');
              li.innerHTML = `${message.user.userid} - ${message.text}`;
              ul.appendChild(li);
           });
           this.forum.appendChild(ul);
           this.forum.scrollTop = this.forum.scrollHeight;
        };       
    } 
   
    loadImage(name) {
       let img = document.createElement('img');
       img.src = 'api/store/' + name + '?width=200';
       this.photo.appendChild(img);
    }
   
    /**
     * Add a new message to the forum
     * 
     * @param {type} message
     * @returns {undefined}
     */
    addMessage(message) {
        fetch('api/messages/add?name=' + this.name,  {
            method: 'POST', 
            body : JSON.stringify(new Message(this.userid,message)),
            headers: {'Content-Type' : 'application/json; charset=UTF-8'},
            credentials: 'same-origin'
        })
        .then(response => {
           if(response.ok) {
                return response.json();
            }

            throw new Error("Failed to send message " + event.target.value);
        })
       .then(message => {
           this.message.value = "";
        })
        .catch(exception => console.log("Error: " + exception));
   }   
}


/**
 * Class representing a message. Make shure no attribute is null.
 * 
 * @type type
 */
class Message {
    constructor(userid, text) {
        this.text = text;
        this.version = null;
        this.user = new User(userid);
    }
}

/**
 * Class representing a user
 * @type type
 */
class User {
    constructor(userid) {
        this.userid = userid;
    }
}

// Instantiate the forum
let forum = new PhotoForum();


