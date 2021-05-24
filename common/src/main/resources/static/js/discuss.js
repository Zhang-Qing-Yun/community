$(function(){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});

function like(btn, entityType, entityId, entityUserId, postId) {
    let entityUserId2 = 0;
    if (typeof (entityUserId) === 'string') {
        entityUserId2 = parseInt(entityUserId);
    } else {
        entityUserId2 = entityUserId;
    }
    $.post(
        "/message/like/likeOne",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId2,"postId":postId},
        function(data) {
            // data = $.parseJSON(data);
            if(data.code === 20000) {
                $(btn).children("i").text(data.data.likeCount);
                $(btn).children("b").text(data.data.likeStatus === 1 ? '已赞' : "赞");
            } else {
                alert(data.message);
            }
        }
    );
}

// 置顶
function setTop() {
    $.post(
        "/community/post/top",
        {"id":$("#postId").val()},
        function(data) {
            if(data.code === 20000) {
                $("#topBtn").attr("disabled", "disabled");
                alert(data.message);
            } else {
                data = $.parseJSON(data);
                alert(data.message);
            }
        }
    );
}

// 加精
function setWonderful() {
    $.post(
        "/community/post/wonderful",
        {"id":$("#postId").val()},
        function(data) {
            if(data.code === 20000) {
                $("#wonderfulBtn").attr("disabled", "disabled");
                alert(data.message);
            } else {
                data = $.parseJSON(data);
                alert(data.message);
            }
        }
    );
}

// 删除
function setDelete() {
    $.post(
        "/community/post/delete",
        {"id":$("#postId").val()},
        function(data) {
            if(data.code === 20000) {
                location.href = "/community/post/index";
            } else {
                data = $.parseJSON(data);
                alert(data.message);
            }
        }
    );
}