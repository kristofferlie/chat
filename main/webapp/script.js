class Slideshow {
    constructor() {
        this.images = document.querySelector("#images");
        this.load();
    }
    
    load() {
        fetch('api/store/images')
            .then(response => {
                if(response.ok) {
                    return response.json();
                }
                
                throw new Error("Failed to load list of images");
            })
            .then(json => this.addImages(json))
            .catch(e => console.log("Error: " + e.message));           
    }
    
    addImages(json) {
        this.images.innerHTML = '';
        for(let i = 0; i < json.length; i++) {
            let img = document.createElement('img');
            img.src = 'api/store/' + json[i].name + '?width=250';
            
            let a = document.createElement('a');
            a.href = "photo.html?name=" + json[i].name;
            a.appendChild(img);            
            
            this.images.appendChild(a);
        }
    }
}
let slideshow = new Slideshow();
