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
$(document).ready(function() {

    showArrow();

    showMotiv();

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


