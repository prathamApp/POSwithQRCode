var DataUsage={};

DataUsage.language={
	'Gujarati':'MuktaVaani-Regular.ttf',
	'Odiya':'Lohit-Oriya.ttf',
	'Bengali':null,
	'Punjabi':'31225_raavi.ttf',
	'Tamil':null,
	'Telugu':null,
	'Hindi':null,
	'Marathi':null,
	'Assamese':'GEETL___.TTF',
	'Kannada':null
	};
	
$(document).ready(function(){
	var languageName=''; 
	languageName=Android.getLanguage();
    DataUsage.json=Android.getConfig();
    DataUsage.json = $.parseJSON('' + DataUsage.json + '');
    console.log(DataUsage.json);
    setTimeout(function(){
        console.log("Selected Language : "+languageName);
        console.log("****************************************************************");
        console.log("Config : "+DataUsage.json);
        if(DataUsage.language[languageName]!==null)
            setFontFamilyForLang("fonts/"+DataUsage.language[languageName]);
        DataUsage.counter=0,DataUsage.arrOfResources=[];
        DataUsage.addData();
	},8000);
});


DataUsage.addData=function()
{
	$('#game').data('type','game');
	$('#video').data('type','video');
	$('#pdf').data('type','pdf');
};


DataUsage.addNode=function(node)
{
	for(var i=0;i<node.length;i++)
		DataUsage.parseNode(node[i]);
};

DataUsage.parseNode=function(node)
{
	var tempObj={};
	if(node.nodeType!=='Resource')
	{
		if(node.nodelist!==null)
			DataUsage.addNode(node.nodelist);
	}
	else
	{
		tempObj.nodeTitle=node.nodeTitle;
		tempObj.resourceId=node.resourceId;
		tempObj.resourceType=node.resourceType;
		DataUsage.arrOfResources.push(tempObj);
	}
};

setFontFamilyForLang=function(fontFilePath){
  var newStyle = "<style>"+
            "@font-face {  "+
              "font-family: 'CustomFontFamily';"  +
                "src: url('"+fontFilePath+"') format('truetype');"+
                "}  </style>";
    $("head").append(newStyle);
    $("#ContentBody2").css("font-family","CustomFontFamily");            
}

DataUsage.showUsedResources=function(element)
{
	var str,tempArr=[];
    console.log("*******************************Calling*********************************");
	str=Android.fetchUsedResources();
	//str = $.parseJSON('' + str + '');
	//tempArr.replace(',',' ');
	tempArr=str.split(',');
	tempArr.splice(tempArr.length-1,1);
    console.log("*******************************Called*********************************");
    console.log("tempArr : "+tempArr);
    console.log("*******************************Called*********************************");

    setTimeout(function(){
        DataUsage.arrOfResources=[];

        DataUsage.addNode(DataUsage.json);
        $('#played,#notPlayed').empty();
        for(var i=0;i<tempArr.length;i++)
        {
            for(var j=0;j<DataUsage.arrOfResources.length;j++)
            {
                if(DataUsage.arrOfResources[j]['resourceId']==tempArr[i]&& DataUsage.arrOfResources[j]['resourceType']==$(element).data('type')&& ((!tempArr[i].includes('Assessment'))||(!tempArr[i].includes('SessionTracking'))))
                {
                    var div=$('<div id="div'+i+'" style="margin:2% 0% 2% 0%;color:black;font-size:medium;font-weight:bold;">'+DataUsage.arrOfResources[j]['nodeTitle']+'</div>');
                    $('#played').append(div);
                    DataUsage.arrOfResources.splice(j,1);

                }

            }

        }
        DataUsage.showNotUsedResources(element);
    },3000);

};

DataUsage.showNotUsedResources=function(element)
{
var i=0;
	for(var j=0;j<DataUsage.arrOfResources.length;j++)
                {
                    if(DataUsage.arrOfResources[j]['resourceType']==$(element).data('type'))
                    {
                    i++;
                        var div=$('<div id="notPlayed'+i+'" style="margin:2% 0% 2% 0%;color:black;font-size:medium;font-weight:bold;">'+DataUsage.arrOfResources[j]['nodeTitle']+'</div>');
                        $('#notPlayed').append(div);
                        DataUsage.arrOfResources.splice(j,1);

                    }

                }
};
