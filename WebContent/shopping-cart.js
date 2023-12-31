
/**
 * Handle the items in item list
 * @param resultData jsonObject, needs to be parsed to html
 */
function handleCartArray(resultData) {
    console.log(resultData);
    let item_table = $("#item_table");
    // change it to html list
    let res = "";
    for (let i = 0; i < resultData.length; i++) {
        // each item will be in a bullet point
        res += "<tr>";
        res += "<th>" + resultData[i]['movie_title'] + "</th>";
        res += "<th>" + "<button ID='" + resultData[i]['movie_id'] + "' ONCLICK=movieDecrement('" + resultData[i]['movie_id'] + "')>-</button>" +
                        "<span ID='page-number'> " + resultData[i]['movie_count'] + " </span>" +
                        "<button ID='" + resultData[i]['movie_id'] + "' ONCLICK=movieIncrement('" + resultData[i]['movie_id'] + "')>+</button>" + "</th>";
        res += "<th>" + "<button ID='" + resultData[i]['movie_id'] + "' ONCLICK=movieDelete('" + resultData[i]['movie_id'] + "')>Delete</button>" + "</th>";
        res += "<th>" + "$" + resultData[i]['movie_price'] + "</th>";
        res += "<th>" + "$" + (resultData[i]['movie_price'] * resultData[i]['movie_count']) + "</th>";
        res += "</tr>";
    }

    // clear the old array and show the new array in the frontend
    item_table.html("");
    item_table.append(res);

    let item_total = $("#item_total");
    res = "";
    if (resultData.length > 0) {
        res += "<p>Total Price: $" + resultData[0]['total_cart_price'] + "</p><br>";
        res += "<form ACTION='purchase-info.html'>\n" +
            "    <input TYPE='submit' VALUE='Proceed to Payment'>\n" +
            "</form>";
    } else {
        res += "<p>Your cart is empty.</p>"
    }
    item_total.html("");
    item_total.append(res);
}

function movieDecrement(id) {
    console.log("Decrementing " + id);
    $.ajax("api/cart", {
        method: "POST",
        data: "id=" + id + "&action=decrement",
        success: function () {
            console.log("movieDecrement SUCCESS");
            $.ajax("api/cart", {
                method: "GET",
                success: handleCartArray
            });
        }
    });
}

function movieIncrement(id) {
    console.log("Incrementing " + id);
    $.ajax("api/cart", {
        method: "POST",
        data: "id=" + id + "&action=increment",
        success: function () {
            console.log("movieIncrement SUCCESS");
            $.ajax("api/cart", {
                method: "GET",
                success: handleCartArray
            });
        }
    });
}

function movieDelete(id) {
    console.log("Deleting " + id);
    $.ajax("api/cart", {
        method: "POST",
        data: "id=" + id + "&action=delete",
        success: function () {
            console.log("movieDelete SUCCESS");
            $.ajax("api/cart", {
                method: "GET",
                success: handleCartArray
            });
        }
    });
}

/**
 * Submit form content with POST method
 * @param cartEvent
 */
function handleCartInfo(cartEvent) {
    console.log("submit cart form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    cartEvent.preventDefault();
    console.log(cart);
    console.log($(this))

    $.ajax("api/cart", {
        method: "POST",
        data: cart.serialize(),
        success: resultDataString => {
            let resultDataJson = JSON.parse(resultDataString);
            handleCartArray(resultDataJson["previousItems"]);
        }
    });

    // clear input form
    cart[0].reset();
}

$.ajax("api/cart", {
    method: "GET",
    success: handleCartArray
});

