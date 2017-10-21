/**
 * Created by Alexzander on 04.10.2017.
 */
var photos = ["images/arm.png", "images/bracerphone.jpg"];
var current = 1;
var arrowOpacity = false;
var mail = {
    from_mail: "",
    from_name: "",
    from_region: "",
    from_phone: ""
};
var langs = [{
    lang: "English",
    path: "../locals/en.xml"
}, {
    lang: "Russian",
    path: "../locals/ru.xml"
}, {
    lang: "French",
    path: "../locals/fr.xml"
}, {
    lang: "Deutsch",
    path: "../locals/de.xml"
}, {
    lang: "German",
    path: "../locals/de.xml"
}];
var lngPos = [
    "English",
    "German",
    "French",
    "Russian"
];
var langIsShown = false;
$(document).ready(function() {

    detectLang(0);

    showArrow();

    showMotiv();

    $(".country").click(function() {
        if (langIsShown) {
            if (!($(this).is($('#lang').children(".country").first()))) {
                detectLang($(this).index());
                swapElement($(this), $('#lang').children(".country").first());
            }
            $("#lang").animate({
                height: '30px'
            }, 400);
            langIsShown = false;
        } else {
            $("#lang").animate({
                height: '120px'
            }, 400);
            langIsShown = true;
        }
        $("#lang").children().each(function() {
            $(this).css('font-family', 'Rus');
        });
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
        showArrow();
    });

    $("#uparrow").click(function() {
        $("html, body").animate({
            scrollTop: 0
        }, "slow");
    })

    $("#productnav").click(function() {
        $("html, body").animate({
            scrollTop: $('#devinfo').offset().top - 150
        }, "slow");
    });

    $("#ordertracker").click(function() {
        $("html, body").animate({
            scrollTop: $('#order').offset().top - 150
        }, "slow");
    });

    $("#cntcts").click(function() {
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

function showArrow() {
    var height = $(window).scrollTop();
    if (height != 0) {
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

function tryDirect() {
    if ((/Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent))) {
        window.location.replace("http://mobile.bloodconnection.epizy.com/");
    }
}

function stringifyXML(url) {
    var xml_string = null;
    $.ajax({
        type: "GET",
        url: url,
        dataType: "text",
        async: false,
        success: function(xml) {
            xml_string = xml;
        },
        error: function(xml) {}
    });
    return xml_string;
}

function detectLang(pos) {
    $.browserLanguage(function(language, acceptHeader) {
        defineLanguage(language, pos);
    });
}

function changeLang(lan) {
    var lagua = lan;
    console.log(lagua);
    if (lagua == "Russian") {
        var newStyle = document.createElement('style');
        newStyle.appendChild(document.createTextNode("\
            @font-face {\
                font-family: 'Rus';\
                src: url('fonts/10771.ttf');\
            }\
            "));
        document.head.appendChild(newStyle);
        $('body').find('*').each(function() {
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
    for (var i = 0; i < localize.length; ++i) {
        var txt = elements[localize[i]];
        if (txt.indexOf("~") != -1) {
            txt = txt.replace(/~/g, "</br>");
        }
        $("#" + localize[i]).html(txt);
    }
}

Array.prototype.selWhereLangEq = function(lang) {
    for (var i = 0; i < langs.length; ++i) {
        if (langs[i].lang == lang) return langs[i].path;
    }
    return null;
}

function swapElement(a, b) {
    var aNext = $('<div>').insertAfter(a);
    a.insertAfter(b);
    b.insertBefore(aNext);
    aNext.remove();
}

function sw(lan) {
    for (var i = 1; i < lngPos.length; ++i) {
        if (lngPos[i] == lan) {
            var tmp = lngPos[0];
            lngPos[0] = lngPos[i];
            lngPos[i] = tmp;
            return i;
        }
    }
}

function defineLanguage(blang, position) {
    var cook = Cookies.get('lang');
    if (cook === undefined) {
        swapElement($("#lang").children().eq(sw(blang)), $('#lang').children(".country").first());
        changeLang(lngPos[0]);
        Cookies.set('lang', lngPos[0], {
            path: ''
        });
    } else {
        swapElement($("#lang").children().eq(sw(cook)), $('#lang').children(".country").first());
        var newStyle = document.createElement('style');
        newStyle.appendChild(document.createTextNode("\
            @font-face {\
                font-family: 'Rus';\
                src: url('fonts/10771.ttf');\
            }\
            "));
        document.head.appendChild(newStyle);
        $('body').find('*').each(function() {
            this.style.setProperty('font-family', 'Rus', '');
        });
        if (cook != lngPos[position]) {
            var tmp = lngPos[0];
            lngPos[0] = lngPos[position];
            lngPos[position] = tmp;
        }
        Cookies.remove('lang', {
            path: ''
        });
        changeLang(lngPos[0]);
        Cookies.set('lang', lngPos[0], {
            path: ''
        });
    }
}
