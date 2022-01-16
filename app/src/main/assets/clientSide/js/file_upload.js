class FileUpload
{
	constructor(filename,pObj,url,audio)
	{
		this.pObj = pObj
		this.audio = audio
		this.url = url
		this.read(filename)
	}

	//routine for reading file and converting to base64
	read(filename)
	{

		let reader = new FileReader()
		reader.addEventListener("load",event=>
		{
			this.upload(event.target.result)
		})
		reader.readAsArrayBuffer(filename)
	}

	upload(data)
	{
		let xhr = new XMLHttpRequest()
		xhr.open("POST", this.url, true)

		//update progress bar
		xhr.upload.addEventListener("progress",event=>
		{
			let percent = (event.loaded/event.total)*100
			this.pObj.style.width = percent+"%"
		})

		//handle upload complete
		xhr.upload.addEventListener("loadend",event=>
		{
			this.audio.play()
		})

		//handle err
		xhr.upload.addEventListener('error', event=> 
		{

    	})

		//sending data to server
		xhr.send(data)
	}
}

export {FileUpload}