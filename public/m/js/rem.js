function Rem(){
	var rem = document.documentElement.clientWidth/7.5;
	document.documentElement.style.fontSize = rem+'px';
	if(document.documentElement.clientWidth>=750){
		document.documentElement.clientWidth=750;
		document.documentElement.style.fontSize = 100+'px';
	}else if(document.documentElement.clientWidth<=320){
		document.documentElement.clientWidth=320;
		document.documentElement.style.fontSize = 320/7.5+'px';
	}
}
Rem()
window.onresize=function(){
	Rem();
};

