let paymentForm = $("#payment-info");

function displayTotalPrice(totalPriceData) {
    let totalPriceHTML = $("#payment-amount");
    let res = "<p>Final Payment: $" + totalPriceData[0]["totalCartPrice"] + "</p>";
    totalPriceHTML.append(res);
}

$.ajax("api/payment", {
    method: "GET",
    success: displayTotalPrice
});

function handleAuthRequest(authData) {
    if (authData[0]['authorized'] == "true") {
        console.log("Correct payment info");
    } else if (authData[0]['authorized'] == "false") {
        console.log("Wrong payment info");
    }
}
function handlePaymentInfo(formEvent) {
    console.log("submit payment form");
    formEvent.preventDefault();

    $.ajax("api/payment", {
        method: "POST",
        data: paymentForm.serialize(),
        success: handleAuthRequest
    });

}

paymentForm.submit(handlePaymentInfo);