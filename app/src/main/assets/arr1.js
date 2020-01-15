toast("即将刷新6")
//swipe(800,300,800,600,1000)
console.show()
bao()
function bao(){
    try {
        var zhangdan = text("账单").findOne(1000).parent().parent().parent().parent().parent().child(2).child(0).child(0).child(1).child(0).child(0).child(0).child(1).children()

        className("ListView").scrollBackward();
        sleep(5000)
//        id("as_inner_list_view").scrollUp()
    } catch (error) {
        launchApp("支付宝")
        sleep(5000)
        text("我的").findOnce().parent().click()
         sleep(5000)
         text("账单").findOnce().parent().parent().parent().parent().parent().click()
         sleep(5000)
         sleep(5000)
         var zhangdan = text("账单").findOne(1000).parent().parent().parent().parent().parent().child(2).child(0).child(0).child(1).child(0).child(0).child(0).child(1).children()

    }
    let str = []
    for(let i = 3 ; i < zhangdan.length;i++){

        let mode1 = zhangdan[i].child(0).child(1).child(0).child(0).getText()  //支付方式
        let balance = zhangdan[i].child(0).child(1).child(0).child(1).getText() //余额
        let time = zhangdan[i].child(0).child(1).child(2).child(0).child(1).getText()  //时间
        let data = zhangdan[i].child(0).child(1).child(2).child(0).child(0).getText() //日期


        let itemStr = new Object()
        itemStr.trading_time = time + ';' + data
        itemStr.order_money = balance
        itemStr.transfer_type = mode1
        itemStr.reciprocal_name = ""
        str.push(itemStr)
        //console.log(flag+"----"+mode + "---"+"---"+balance+"---"+time + "---" + money)//支付方式
        console.log(mode1+"---"+balance+"---"+time+"---"+data)
        toast(mode1+"---"+balance+"---"+time+"---"+data);
    }

    let cdData = JSON.stringify( str );
    toast(cdData);
    if (str.length > 0) {
        toast("数据传输中。。。")
        var action="com.submit.data"
            app.sendBroadcast(
              {
                action:action,
                extras:{
                  sumitData:cdData
                }
              }
            );
    }



}

