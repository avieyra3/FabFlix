function renderSalesTable(resultData) {
    console.log(resultData);
    let item_table = $("#item_table");
    // change it to html list
    let res = "";
    for (let i = 0; i < resultData.length; i++) {
        // each item will be in a bullet point
        res += "<tr>";
        res += "<th>" + resultData[i]['sale_id'] + "</th>";
        res += "<th>" + resultData[i]['movie_title'] + "</th>";
        res += "<th>" + resultData[i]['movie_count'] + "</th>";
        res += "<th>" + "$" + resultData[i]['movie_price'] + "</th>";
        res += "<th>" + "$" + (resultData[i]['movie_price'] * resultData[i]['movie_count']) + "</th>";
        res += "</tr>";
    }
    item_table.append(res);

    $("#item_total").append("<p>Total Price: $" + resultData[0]['total_cart_price'] + "</p><br>");
}

$.ajax("api/confirm", {
    method: "GET",
    success: renderSalesTable
});