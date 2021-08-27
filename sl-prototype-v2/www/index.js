

let net;
let imageFile;
let letters;
var img = new Image();


async function app(){ 
 img.src = imageFile;
 const result = await net.classify(img);
 let maxx = 0;
 
 console.log(result);  
 for (var key in result) {
    if(Math.round(result[key]['prob']*100) > maxx){
        maxx = Math.round(result[key]['prob']*100);
        letters = result[key]['label'];
    }
 }
 document.getElementById("result").innerHTML=letters;
   
 await tf.nextFrame();
}

async function load_modell(){
    
 console.log('Loading model..');
 net= await tf.automl.loadImageClassification('model.json');
 console.log('Successfully loaded model');
};
