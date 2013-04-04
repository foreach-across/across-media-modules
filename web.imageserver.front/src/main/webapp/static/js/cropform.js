dojo.declare( "CropForm", null,
{
	haveAllFormats : false,
	minWidthNeeded : 0,
	allFormatsStatus : 0,
	baseUrl : null,
	numAspectRatio : 0,
	aspectRatio : 0,
	root : null,
	dcId : 0,
	selectedFormatId : 0,
	haveAll : false,
	imageWidth : 0,
	imageFullWidth : 0,
	imageHeight : 0,
	imageCropX : 0,
	imageCropY : 0,
	imageCropW : 0,
	imageCropH : 0,
	formatsArray : [],
	busy : false,

	constructor: function(paspectRatioFactor,
					  pminimumWidthNeeded,
					  pallFormatsStatus,
					  pbaseUrl,
					  pnumAspectRatio,
					  proot,
					  pdcId,
					  pselectedFormatId,
					  phaveAll,
					  pimageWidth,
					  pimageFullWidth,
					  pimageHeight,
					  formats,
					  pimageCropX,
					  pimageCropY,
					  pimageCropW,
					  pimageCropH)
	{
		this.minWidthNeeded = parseInt(pminimumWidthNeeded,10);
		this.allFormatsStatus = pallFormatsStatus;
		this.baseUrl = pbaseUrl;
		this.numAspectRatio = parseInt(pnumAspectRatio,10);
		this.root = proot;
		this.dcId = parseInt(pdcId,10);
		this.selectedFormatId = pselectedFormatId;
		this.haveAll = phaveAll;
		this.imageWidth = parseInt(pimageWidth,10);
		this.imageFullWidth = parseInt(pimageFullWidth,10);
		this.imageHeight = parseInt(pimageHeight,10);
		this.formatsArray = formats.split(",");

		Cropper.init();
		this.aspectRatio = paspectRatioFactor;
		Cropper.setRatio(this.aspectRatio);

		if (pimageCropW && pimageCropW > 0)
		{
			Cropper.formSetX(parseInt(pimageCropX,10));
			Cropper.formSetY(parseInt(pimageCropY,10));
			Cropper.formSetW(parseInt(pimageCropW,10));
			Cropper.formSetH(parseInt(pimageCropH,10));
		}
		else
		{
			Cropper.selectMaxAspectRatio();
		}

		this.setAllFormatsCheckbox(this.allFormatsStatus);

	},

	cropCallback : function()
	{
		// do nothing fornow
	},

	save : function(){
		this.beforeSubmit();
	},

	clearData : function(){
		Cropper.reset();
	},


	beforeSubmit : function(){
		var x = document.getElementById("cropX").value;
		var y = document.getElementById("cropY").value;
		var w = document.getElementById("cropW").value;
		var h = document.getElementById("cropH").value;

		//set the co-ordinates to 0 if they are empty, so that we can avoid spring binding exception
		if (x == '') document.getElementById("cropX").value = '0';
		if (y == '') document.getElementById("cropY").value = '0';
		if (w == '') document.getElementById("cropW").value = '0';
		if (h == '') document.getElementById("cropH").value = '0';
	},

	doPrev : function()
	{
		if (this.busy)
			return;

		this.busy = true;

		if ( this.haveAllFormats )
			switchPrev(this.baseUrl, this.numAspectRatio);
		else
		{
			var t = this;
			this.cropMissing(function()
			{
				switchPrev(t.baseUrl ,t.numAspectRatio);
			});
		}
	},





	doNext : function()
	{
		if (this.busy)
			return;

		this.busy = true;

		if ( this.haveAllFormats )
			switchNext(this.baseUrl, this.numAspectRatio);
		else
		{
			var t = this;
			this.cropMissing(function()
			{
				switchNext(t.baseUrl ,t.numAspectRatio);
			});
		}
	} ,

	doClose : function()
	{
		if (this.busy)
			return;

		this.busy = true;

		if (this.haveAllFormats)
			closeCropDialog(this.dcId);
		else
		{
			var t = this;
			this.cropMissing(function()
			{
				closeCropDialog(t.dcId);
			});
		}
	} ,

	/*function checkHaveAll()
	{
		return haveAllFormats || cropMissing() || confirm("You haven't defined all crop formats for this aspect ratio. Continue?");
	}*/

	renderFormat : function(){
		var formatAction = document.getElementById("cropForm").formatAction;
		var fValue = formatAction.value;
		var action = this.root + "/images/renderFormatPreview?dcId="+ this.dcId + "&action=";

		if (fValue == '1'){
			// option show is selected, so we should not use crop co-ordinates, reset them
			Cropper.reset();
			this.beforeSubmit();

			document.getElementById("cropForm").action = action + "show";
			formatAction.selectedIndex = '0';
			return doAjaxCall( { form: dojo.byId( 'cropForm' ), container: 'cropPreviewContainer' } );
		}else if (fValue == '2'){
			this.beforeSubmit();

			if (document.getElementById("cropW").value == '0' || document.getElementById("cropH").value == '0'){
				alert("Please select a crop area to preview");
				document.getElementById("cropForm").formatAction.selectedIndex = 0;
				return;
			}

			document.getElementById("cropForm").action = action + "setFromSelection";
			formatAction.selectedIndex = '0';
			return doAjaxCall( { form: dojo.byId( 'cropForm' ), container: 'cropPreviewContainer' } );
		}
	},

	doCrop : function()
	{
        if (this.busy)
			return;

		this.busy = true;

        // TODO-pjs(20110629): we need to copy this logic to the backend.

		if ((this.getRealWidth() + this.getScaleFactor() / (3.0 / 2.0)) < this.minWidthNeeded)
		{
			alert("real width of selection is less than " + this.minWidthNeeded +". Will not save because this will lead to quality loss.");
			this.busy = false;
			return false;
		}

        //synchronous

		//var span = dojo.byId('cropOkSpan_' + this.selectedFormatId);
		//dojo.removeClass(span,'cropOk');
		//dojo.addClass(span,'cropBusy');

        this.busy = false;
		return true;
	},

	cropMissing : function(callback)
	{
//		if ((getRealWidth() + getScaleFactor() / (3.0 / 2.0)) < minWidthNeeded)
//		{
//			//alert("real width of selection is less than " + minWidthNeeded +". Will not save because this will lead to quality loss.");
//			//return false;
//			if (confirm("You haven't defined all crop formats for this aspect ratio. Continue?"))
//				callback();
//
//		}
//		else
//		{

			doAjaxCall(
			{
				callback: callback,
				form: dojo.byId( 'cropForm' ),
				params: { onlyMissing: true, fullWidth: this.imageFullWidth },
				background: true
			}
			);
		//}
	},



	setAllCheckboxes: function(formatsStati)
	{
		if (!this.haveAll)
			return;

		for (var i in this.formatsArray )
		{
			var formatId = this.formatsArray[i];
			var tempSpan = dojo.byId('cropOkSpan_' + formatId);

			if (formatsStati)
			{
				var formatStatus = formatsStati[i];
				if (formatStatus == 'OK' || formatStatus == 'DIFF')
					dojo.addClass(tempSpan, 'cropOk');
				else
					dojo.removeClass(tempSpan, 'cropOk');

				if (formatStatus == 'DIFF')
					dojo.addClass(tempSpan, 'diff');
				else
					dojo.removeClass(tempSpan, 'diff');
			}
			else
			{
				dojo.addClass(tempSpan,'cropOk');
				dojo.removeClass(tempSpan,'diff');
			}
		}

	},

	setAllFormatsCheckbox : function(allFormatsStatus)
	{
	    this.haveAllFormats = allFormatsStatus != 'NOK';

		if (!this.haveAll)
			return;

		var allSpan = dojo.byId('cropOkSpan_all');

		if (allFormatsStatus == 'OK' || allFormatsStatus == 'DIFF')
			dojo.addClass(allSpan, 'cropOk');
		else
			dojo.removeClass(allSpan, 'cropOk');

		if (allFormatsStatus == 'DIFF')
			dojo.addClass(allSpan, 'diff');
		else
			dojo.removeClass(allSpan, 'diff');



	},

    /*
	switchBrand : function()
	{
		if (this.busy)
			return;

		this.busy = true;

		var brandSelect = document.getElementById("cropForm").brandSelect;
		var brandId = brandSelect.value;
		var newUrl = this.root + '/images/cropForm?dcId='+this.dcId+'&brandId=' + brandId;
		doAjaxCall( { url: newUrl } );
		return false;
	},*/


	getImageViewHeight : function()
	{
		return parseInt(document.getElementById("imageH").value,10);
	},

	getImageViewWidth : function()
	{
		return parseInt(document.getElementById("imageW").value,10);
	},

	getCropX : function()
	{
		return parseInt(document.getElementById("left").value,10);
	},

	getCropY : function()
	{
		return parseInt(document.getElementById("top").value,10);
	},

	getCropHeight : function()
	{
		return parseInt(document.getElementById("height").value,10);
	},

	getCropWidth : function()
	{
		return parseInt(document.getElementById("width").value,10);
	},

	getScaleFactor : function()
	{
		return this.getFullWidthForRatio() / this.getImageViewWidth();
	},

	getWidth : function()
	{
		return this.imageWidth;
	},

	getMaxWidthForRatio : function()
	{
		if (this.imageWidth / this.aspectRatio > this.imageHeight)
			return this.imageHeight * this.aspectRatio;
		else
			return this.imageWidth;
	},

	getFullWidthForRatio : function()
	{
		if (this.minWidthNeeded > this.getMaxWidthForRatio()) // only return fullwidth when really needed (e.g. W468 on portrait image)
		{
			if (this.imageFullWidth <= 0)
				return this.getMaxWidthForRatio();
			return this.imageFullWidth;
		}
		else
			return this.getMaxWidthForRatio();
	},

	getHeight : function()
	{
		return this.imageHeight;
	},


	getRealWidth : function()
	{
		if (document.getElementById("cropW").value == '')
		{
			return this.getFullWidthForRatio();
		}
		else
		{
			return this.getCropWidth() * this.getScaleFactor();
		}
	}


});

function switchPrev (baseUrl, numAspectRatio)
{
	//alert('switchPrev');
	doAjaxCall( { url: baseUrl + '/' + (numAspectRatio - 1) } );
}

function switchNext (baseUrl, numAspectRatio)
{
	//alert('switchNext');
	doAjaxCall( { url: baseUrl + '/' + (numAspectRatio + 1) } );
}

function closeCropDialog (dcId)
{
	ui.activeDialog.close();
	reloadImagePanes(dcId);
}

function setCropOk (success, response, cropForm)
	{
		if ( success ) {
			var span = dojo.byId('cropOkSpan_' + cropForm.selectedFormatId);
			dojo.removeClass(span,'cropBusy');

			var result = dojo.fromJson(response);
			cropForm.setAllFormatsCheckbox(result.allStatus);

			if (cropForm.selectedFormatId == 'all')
			{
				cropForm.setAllCheckboxes();
			}
			else if (cropForm.haveAll)
			{
				cropForm.setAllCheckboxes(result.formats);
			}
			else
			{
				dojo.addClass(span,'cropOk');
			}
		}

		else
			alert( "Could not crop image." );

		cropForm.busy = false;

	}
