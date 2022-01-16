import { FileUpload } from "./file_upload.js"

let url 
let file = document.querySelector(".file")
let pro = document.querySelector(".inner")
let audio = new Audio("audio/sent_a.wav")

//add event listener to input file
file.addEventListener("change",event=>
{
	let file = event.target.files[0]
	url = window.location.origin+"/"+ event.target.files[0].name
	let fileUpload = new FileUpload(file,pro,url,audio)
})
