AJS.$(function () {
    AJS.$(".aui-nav li").removeClass("aui-nav-selected");
    AJS.$("#mo-idps").addClass("aui-nav-selected");
    AJS.$("#mo-saml").addClass("aui-nav-selected");
});

function searchIDP() {
    var input, filter, grid,moidp,a, i;
    input = document.getElementById("search");
    filter = input.value.toLowerCase();
    grid = document.getElementById("mo-grid");
    moidp =  grid.getElementsByTagName("div");
    for (i = 0; i < moidp.length; i++) {
        a = moidp[i].getElementsByTagName("a")[0];
        if (a.id.toLowerCase().indexOf(filter)===0) {
            moidp[i].style.display = "";
        } else {
            moidp[i].style.display = "none";
        }
    }
}