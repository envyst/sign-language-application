// const webcamElement= document.getElementById('webcam');
// let isPredicting = false;
// function startPredicting(){
//  isPredicting=true;
//  app();
// }
// function stopPredicting(){
//  isPredicting=false;
//  app();
// }

let net;
console.log('Loading model..');
net= await tf.automl.loadImageClassification('model.json');
console.log('Successfully loaded model');
let imageFile;
let letters;
imageUpload.addEventListener('change', function(e){
    imageFile = e.target.files;
});

async function app(){ 
//  const webcam = await tf.data.webcam(webcamElement);
//  while(isPredicting){
//  const img = await webcam.capture();
 const result = await net.classify(imageFile);
 let maxx = 0;
 
 console.log(result);
 for(let x = 0; x < result.length; x++){
     let index = x.toString();
     if(result[index]['prob'] > maxx){
         maxx = result[index]['prob'];
         letters = result[index]['label'];
     }
 }
 
//  document.getElementById("result").innerText=letters;
//  imageFile.dispose();
 
 await tf.nextFrame();
 
//  }
 
}