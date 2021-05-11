function like(btn, entityType, entityId, entityUserId) {
    let entityUserId2 = 0;
    if (typeof (entityUserId) === 'string') {
        entityUserId2 = parseInt(entityUserId);
    } else {
        entityUserId2 = entityUserId;
    }
    $.post(
        "/message/like/likeOne",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId2},
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