

function initImages() {
	var images = document.getElementsByTagName('img');
	for(var i=0; i<images.length; i++) {
		
		images[i].removeAttribute('class');
		images[i].removeAttribute('style');
		images[i].removeAttribute('width');
		images[i].removeAttribute('height');
		images[i].style.display = "block";
		images[i].onerror=function()
		{	
			this.style.display="none";
		}
		if(images[i].parentNode && images[i].parentNode.tagName == "A") {
			images[i].parentNode.setAttribute("href", 'javascript:void(0);');
			images[i].parentNode.setAttribute('onclick','U148.onImageClick("'+ images[i].src +'")');
			if(images[i].parentNode.parentNode.tagName=="P" && images[i].parentNode.parentNode.childNodes.length==1){
				images[i].parentNode.parentNode.style.textIndent ='0em';
			}
		}else{
			images[i].setAttribute('onclick','U148.onImageClick("'+ images[i].src +'")');
			if(images[i].parentNode.tagName=="P" && images[i].parentNode.childNodes.length==1){
				images[i].parentNode.style.textIndent ='0em';
			}
		}

	}
}

function loadData(){
	init();
	window.onChange = init;
}

function init() {
    content = '<div class="content-loading">loading...</div>';
    document.getElementById('content').innerHTML = content;
}

function setArticle(p1, p2) {
    document.getElementById('title').innerHTML = p1.title;
    document.getElementById('content').innerHTML = p2.content;
    initImages();
}

