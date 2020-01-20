//toast("即将刷新7")
//swipe(800,300,800,600,1000)
console.show()
bao()
function bao(){
    let m_str_time = ''
    let m_str_price = ''
    try {

        var payMoney = text("个人收钱").findOnce().getText()

        sleep(5000)
//        toast('payMoney = ' + payMoney)
        if (null != payMoney && '' != payMoney) {
        let payment = text("收款记录").findOnce().parent().child(2).getText()
        if (null != payment && '' != payment) {
            m_str_time = payment.split('收款')[0]
            let price = payment.split('收款')[1]
            m_str_price = price.split('元')[0]
//            toast('---m_str_time = ' + m_str_time + '--- m_str_price = ' + m_str_price)
        } else {
//            toast('无收款记录')
        }
    }
//        id("as_inner_list_view").scrollUp()
    } catch (error) {
        launchApp("支付宝")
        sleep(5000)
        text("首页").findOnce().parent().click()
         sleep(5000)
         text("收钱").findOnce().parent().click()
         sleep(5000)
        let payment = text("收款记录").findOnce().parent().child(2).getText()
          if (null != payment && '' != payment) {
              m_str_time = payment.split('收款')[0]
              let price = payment.split('收款')[1]
              m_str_price = price.split('元')[0]
//              toast('---m_str_time = ' + m_str_time + '--- m_str_price = ' + m_str_price)
          } else {
//            toast('无收款记录')
          }
    }

//    toast('---m_str_time = ' + m_str_time + '--- m_str_price = ' + m_str_price)
    if ('' != m_str_time && '' != m_str_price) {
        let str = []
            let itemStr = new Object()
            itemStr.trading_time = m_str_time
            itemStr.order_money = m_str_price
            let transferType = new Object();
            transferType.mSpanCount = '0'
            transferType.mSpanData = []
            transferType.mSpans = []
            transferType.mText = ''
            itemStr.transfer_type = transferType
            itemStr.reciprocal_name = ""
            str.push(itemStr)

            let cdData = JSON.stringify( str );
//            toast(cdData);
            if (str.length > 0) {
//               toast("数据传输中。。。")
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
    } else {
//        toast('没抓到数据')
    }

}

