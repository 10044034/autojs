
bao()
function bao(){
    try {
        var zhangdan = text("账单").findOne(1000).parent().parent().parent().parent().parent().child(2).child(0).child(0).child(1).child(0).child(0).child(0).child(1).children()
       
    } catch (error) {
        launchApp("支付宝")
        sleep(3000)
        text("我的").findOnce().parent().click()
         sleep(1000)
         text("账单").findOnce().parent().parent().parent().parent().parent().click()
         sleep(1000)
         var zhangdan = text("账单").findOne(1000).parent().parent().parent().parent().parent().child(2).child(0).child(0).child(1).child(0).child(0).child(0).child(1).children()
        
    }
        var id = setInterval(function(){
            
                toast("即将刷新")
                swipe(800,300,800,600,1000)
                console.show()
            for(var i = 3 ; i < zhangdan.length;i++){
        
             var mode = zhangdan[i].child(0).child(1).child(0).child(0).getText()  //支付方式
             var balance = zhangdan[i].child(0).child(1).child(0).child(1).getText() //余额
             var time = zhangdan[i].child(0).child(1).child(2).child(0).child(1).getText()  //时间
             var data = zhangdan[i].child(0).child(1).child(2).child(0).child(0).getText() //日期

             //console.log(flag+"----"+mode + "---"+"---"+balance+"---"+time + "---" + money)//支付方式
             console.log(mode+"---"+balance+"---"+time+"---"+data)
             toast(mode+"---"+balance+"---"+time+"---"+data);
             
     
     }
     console.log("----------------------------------------")
       
            
        }, 5000);

}

