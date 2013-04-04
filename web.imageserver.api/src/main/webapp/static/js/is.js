dojo.require("dijit.Dialog");

var IS     = IS || {};
IS.overlay = null;

IS.utils   = (function(){

    function destroyOverlay() {
        IS.overlay.destroyRecursive();
        IS.overlay.destroyRendering();
        IS.overlay = null;
    }

    return {
        popup : function (e){
            if( IS.overlay ){
                var overlayTitle = IS.overlay.title;
                destroyOverlay();
            }
            IS.overlay = new dijit.Dialog({
                title     : overlayTitle || dojo.attr(e.target, 'title') ,
                autofocus : false,
                href      : dojo.attr(e.target, 'href'),
                class     : 'is-overlay'
            });
            IS.overlay.show();
            IS.overlay.connect(IS.overlay, 'onHide', function() {
                setTimeout(destroyOverlay, 0);
            });

            IS.overlay.connect(IS.overlay, 'onLoad', function(data) {
                dojo.query('.is-overlay a.popup').forEach(
                    function(item, index, array){
                        dojo.connect(item, 'onclick', IS.utils.popup);
                    }
                );
            });
            e.preventDefault();
        }
    }

})();



dojo.addOnLoad( function() {
    
    dojo.query('a.popup').forEach(
        function(item, index, array){
           dojo.connect(item, 'onclick', IS.utils.popup);
        }
    );

});
