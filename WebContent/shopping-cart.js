
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
        res += "<th>" + "<form ACTION='#' id='quantity' METHOD='GET'>\n" +
            "    <input TYPE='submit' NAME='quantity' VALUE='-'>\n" +
            "    <span ID='page-number'> " + resultData[i]['movie_count'] + " </span>\n" +
            "    <input TYPE='submit' NAME='quantity' VALUE='+'>\n" +
            "</form>" + "</th>";
        res += "<th>" + "<form ACTION='#' id='quantity' METHOD='GET'>\n" +
            "    <input TYPE='submit' NAME='quantity' VALUE='Delete'>\n" +
            "</form>" + "</th>";
        res += "<th>" + "$" + "</th>";
        res += "<th>" + "$" + "</th>";
        res += "</tr>";
    }

    
    // clear the old array and show the new array in the frontend
    item_table.html("");
    item_table.append(res);
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

