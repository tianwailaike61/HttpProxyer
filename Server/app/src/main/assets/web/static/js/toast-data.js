/*Toast Init*/


$(document).ready(function() {
	"use strict";
	
	$.toast({
		heading: 'Welcome to kenny',
		text: 'Use the predefined ones, or specify a custom position object.',
		position: 'top-right',
		loaderBg:'#3cb878',
		icon: 'error',
		hideAfter: 3000, 
		stack: 6
	});
	
	$('.tst1').on('click',function(e){
	    $.toast().reset('all'); 
		$("body").removeAttr('class');
		$.toast({
            heading: 'Welcome to kenny',
            text: 'Use the predefined ones, or specify a custom position object.',
            position: 'top-right',
            loaderBg:'#3cb878',
            icon: 'info',
            hideAfter: 3000, 
            stack: 6
        });
		return false;
    });

	$('.tst2').on('click',function(e){
        $.toast().reset('all');
		$("body").removeAttr('class');
		$.toast({
            heading: 'Welcome to kenny',
            text: 'Use the predefined ones, or specify a custom position object.',
            position: 'top-right',
            loaderBg:'#3cb878',
            icon: 'warning',
            hideAfter: 3500, 
            stack: 6
        });
		return false;
	});
	
	$('.tst3').on('click',function(e){
        $.toast().reset('all');
		$("body").removeAttr('class');
		$.toast({
            heading: 'Welcome to kenny',
            text: 'Use the predefined ones, or specify a custom position object.',
            position: 'top-right',
            loaderBg:'#3cb878',
            icon: 'success',
            hideAfter: 3500, 
            stack: 6
          });
		return false;  
	});

	$('.tst4').on('click',function(e){
		$.toast().reset('all');
		$("body").removeAttr('class');
		$.toast({
            heading: 'Welcome to kenny',
            text: 'Use the predefined ones, or specify a custom position object.',
            position: 'top-right',
            loaderBg:'#3cb878',
            icon: 'error',
            hideAfter: 3500
        });
		return false;
    });
	
	$('.tst5').on('click',function(e){
	    $.toast().reset('all');   
		$("body").removeAttr('class');
		$.toast({
            heading: 'top left',
            text: 'Use the predefined ones, or specify a custom position object.',
            position: 'top-left',
            loaderBg:'#3cb878',
            icon: 'error',
            hideAfter: 3500
        });
		return false;
    });
	
	$('.tst6').on('click',function(e){
		$.toast().reset('all');
		$("body").removeAttr('class');
		$.toast({
            heading: 'top right',
            text: 'Use the predefined ones, or specify a custom position object.',
            position: 'top-right',
            loaderBg:'#3cb878',
            icon: 'error',
            hideAfter: 3500
        });
		return false;
    });
	
	$('.tst7').on('click',function(e){
		$.toast().reset('all');
		$("body").removeAttr('class');
		$.toast({
            heading: 'bottom left',
            text: 'Use the predefined ones, or specify a custom position object.',
            position: 'bottom-left',
            loaderBg:'#3cb878',
            icon: 'error',
            hideAfter: 3500
        });
		return false;
    });
	
	$('.tst8').on('click',function(e){
	    $.toast().reset('all');   
		$("body").removeAttr('class');
		$.toast({
            heading: 'bottom right',
            text: 'Use the predefined ones, or specify a custom position object.',
            position: 'bottom-right',
            loaderBg:'#3cb878',
            icon: 'error',
            hideAfter: 3500
        });
		return false;
	});
	
	$('.tst9').on('click',function(e){
	    $.toast().reset('all');   
		$("body").removeAttr('class').removeClass("bottom-center-fullwidth").addClass("top-center-fullwidth");
		$.toast({
            heading: 'top center',
            text: 'Use the predefined ones, or specify a custom position object.',
            position: 'top-center',
            loaderBg:'#3cb878',
            icon: 'error',
            hideAfter: 3500
        });
		return false;
	});
	
	$('.tst10').on('click',function(e){
	    $.toast().reset('all');
		$("body").removeAttr('class').addClass("bottom-center-fullwidth");
		$.toast({
            heading: 'bottom right',
            text: 'Use the predefined ones, or specify a custom position object.',
            position: 'bottom-center',
            loaderBg:'#3cb878',
            icon: 'error',
            hideAfter: 3500
        });
		return false;
	});


//	function syntaxHighlight(json) {
//        if (typeof json != 'string') {
//            json = JSON.stringify(json, null, 2);
//
//        }
//        json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
//        return json.replace(/((\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\])*(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function(match) {
//            var cls = 'number';
//            if (/^/.test(match)) {
//                if (/:$/.test(match)) {
//                    cls = 'key';
//                } else {
//                    cls = 'string';
//                }
//
//            } else if (/true|false/.test(match)) {
//                cls = 'boolean';
//            } else if (/null/.test(match)) {
//                cls = 'null';
//            }
//            return '<span class="' + cls + '">' + match + '</span>';
//        });
//    }
});
          
