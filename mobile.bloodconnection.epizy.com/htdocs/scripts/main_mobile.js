/**
 * Created by Alexzander on 04.10.2017.
 */
var photos = ["images/arm.png", "images/bracerphone.jpg"];
var current = 1;
var arrowOpacity = false;
var menuStatus = false;
var mail = {
    from_mail: "",
    from_name: "",
    from_region: "",
    from_phone: ""
};
var langs = [
    { lang: "English", path: "../locals/en.xml" },
    { lang: "Russian", path: "../locals/ru.xml" },
    { lang: "French",  path: "../locals/fr.xml" },
    { lang: "Deutsch", path: "../locals/de.xml" }
];
$(document).ready(function() {

    detectLang();

    $(".sideinfo").disableSelection();

    $(window).trigger('scroll');

    showMotiv();

    $("#menu").click(function() {
        if (menuStatus == false) {
            $("#nav").css("visibility", "visible");
            menuStatus = true;
        } else {
            $("#nav").css("visibility", "hidden");
            menuStatus = false;
        }
    });

    $(".device").on("doubletap", function(event) {
        event.preventDefault();
        var photos = $(this).find($(".dim"));
        var texts = $(this).find($(".sideinfo"));
        if (photos.css("opacity") == 1) {
            photos.animate({
                opacity: '0'
            }, 400, function() {
                photos.css("visibility", "hidden");
                texts.css("visibility", "visible");
                texts.animate({
                    opacity: '1'
                }, 400);
            });
        } else {
            texts.animate({
                opacity: '0'
            }, 400, function() {
                texts.css("visibility", "hidden");
                photos.css("visibility", "visible");
                photos.animate({
                    opacity: '1'
                }, 400);
            });
        }
    });

    $.widget("custom.iconselectmenu", $.ui.selectmenu, {
        _renderItem: function(ul, item) {
            var li = $("<li>"),
                wrapper = $("<div>", {
                    text: item.label
                });

            if (item.disabled) {
                li.addClass("ui-state-disabled");
            }

            $("<span>", {
                style: item.element.attr("data-style"),
                "class": "ui-icon " + item.element.attr("data-class")
            })
                .appendTo(wrapper);

            return li.append(wrapper).appendTo(ul);
        }
    });

    $("#orderer").click(function(event) {
        if (!$("#namer")[0].checkValidity()) {
            $("#namer")[0].reportValidity();
            return;
        } else {
            if (!$("#pnum")[0].checkValidity()) {
                $("#pnum")[0].reportValidity();
                return;
            } else {
                if (!$("#mail")[0].checkValidity()) {
                    $("#mail")[0].reportValidity();
                    return;
                } else {
                    sendOrder();
                }
            }
        }
    });


    $("#regions").iconselectmenu().iconselectmenu("menuWidget").addClass("ui-menu-icons customicons");

    $(".datafield input:text, input:password").button().css({
        'font': 'inherit',
        'color': 'inherit',
        'text-align': 'left',
        'outline': 'none',
        'cursor': 'text'
    });

    $(window).scroll(function() {
        showArrow($(this));
    });

    $("#uparrow").click(function() {
        $("html, body").animate({
            scrollTop: 0
        }, "slow");
    })

     $("#productnav").click(function() {
         $("#menu").trigger("click");
         $("html, body").animate({
         scrollTop: $('#devinfo').offset().top - 150
         }, "slow");
     });

     $("#ordertracker").click(function() {
         $("#menu").trigger("click");
         $("html, body").animate({
         scrollTop: $('#order').offset().top - 150
         }, "slow");
     });

     $("#cntcts").click(function() {
         $("#menu").trigger("click");
         $("html, body").animate({
         scrollTop: $('#company').offset().top - 150
         }, "slow");
     });

    setInterval(function() {
        $("#photo").animate({
            opacity: '0'
        }, 500, function() {
            var path = photos[current++];
            current %= 2;
            $("#changer").attr("src", path);
            $("#photo").animate({
                opacity: '1'
            }, 500);
        });
    }, 7000);

    $("#dialog-message").dialog({
        autoOpen: false,
        modal: true,
        buttons: {
            Ok: function() {
                $(this).dialog("close");
            }
        },
        show: {
            effect: "blind",
            duration: 800
        },
        hide: {
            effect: "blind",
            duration: 400
        }
    });


});

function showArrow(thise) {
    var height = $(window).scrollTop();
    if (height != 0 && !gotIt(thise)) {
        if (arrowOpacity == false) {
            $("#uparrow").css('visibility', 'visible');
            $("#uparrow").animate({
                opacity: '1'
            }, 500);
            arrowOpacity = true;
        }
    } else {
        if (arrowOpacity == true) {
            $("#uparrow").animate({
                opacity: '0'
            }, 500, function() {
                $("#uparrow").css('visibility', 'visible');
                arrowOpacity = false;
            });
        }
    }
}

(function($) {

    $.event.special.doubletap = {
        bindType: 'touchend',
        delegateType: 'touchend',

        handle: function(event) {
            var handleObj = event.handleObj,
                targetData = jQuery.data(event.target),
                now = new Date().getTime(),
                delta = targetData.lastTouch ? now - targetData.lastTouch : 0,
                delay = delay == null ? 300 : delay;

            if (delta < delay && delta > 30) {
                targetData.lastTouch = null;
                event.type = handleObj.origType;
                ['clientX', 'clientY', 'pageX', 'pageY'].forEach(function(property) {
                    event[property] = event.originalEvent.changedTouches[0][property];
                })

                // let jQuery handle the triggering of "doubletap" event handlers
                handleObj.handler.apply(this, arguments);
            } else {
                targetData.lastTouch = now;
            }
        }
    };

})(jQuery);

function showMotiv() {
    setTimeout(function() {
        $("#worders").animate({
            opacity: '1'
        }, 1000);
    }, 700);
}

function sendOrder() {
    mail.from_mail = $("#mail").val();
    mail.from_name = $("#namer").val();
    mail.from_phone = $("#pnum").val();
    mail.from_region = $("#regions").val();
    emailjs.send("gmail", "bloodconnection", mail)
        .then(function(response) {
            $("#dialog-message").dialog("open");
        }, function(err) {
            console.log("FAILED. error=", err);
        });
}

function gotIt(thise) {
    var hT = $('#company').offset().top,
        hH = $('#company').outerHeight(),
        wH = $(window).height(),
        wS = thise.scrollTop();
    return (wS > (hT + hH - wH));
}

function tryDirect() {
    if(!(/Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent))) {
        window.location.replace("http://web.bloodconnection.epizy.com/");
    }
}

function stringifyXML(url) {
    var xml_string = null;
    $.ajax({
        type: "GET",
        url: url,
        dataType: "text",
        async: false,
        success: function (xml) {
            xml_string = xml;
        },
        error: function (xml) { }
    });
    return xml_string;
}

function detectLang() {
    $.browserLanguage(function( language , acceptHeader ){
        changeLang(language);
    });
}

function changeLang(lan) {
    var lagua = lan;
    console.log(lagua);
    if(lagua == "Russian") {
        var newStyle = document.createElement('style');
        newStyle.appendChild(document.createTextNode("\
            @font-face {\
                font-family: 'Rus';\
                src: url('fonts/10771.ttf');\
            }\
            "));
        document.head.appendChild(newStyle);
        $('body').find('*').each(function () {
            this.style.setProperty('font-family', 'Rus', '');
        });
    }
    var path = langs.selWhereLangEq(lagua);
    var xml = stringifyXML(path);
    var x2js = new X2JS();
    var jsonObj = x2js.xml_str2json(xml);
    manageLanguage(jsonObj);
}

function manageLanguage(obj) {
    var elements = obj.root;
    var localize = Object.keys(elements);
    for(var i = 0; i < localize.length; ++i) {
        var txt = elements[localize[i]];
        if(txt.indexOf("~") != -1) {
            txt = txt.replace(/~/g,"</br>");
        }
        $("#" + localize[i]).html(txt);
    }
}

Array.prototype.selWhereLangEq = function (lang) {
    for(var i = 0; i < langs.length; ++i) {
        if(langs[i].lang == lang) return langs[i].path;
    }
    return null;
}
