1.断线重连可选，默认没开为false，创建客户端之后可以设为true
2.client发送文件失败之后将记录到redis，包含本次文件内容，

3.如果开启重连，那么如果重连成功就更新连接状态，同时发送消息到kafka，当卡夫卡监听到消息，知道服务器可用了就到redis里面取出数据重新发送。
4.如果开启重连，重连失败可以关闭client，也可以不做什么事
5.会有定时任务处理kakfa重发失败消息的二次失败数据。