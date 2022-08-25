var websock = null;
var wsuri =import.meta.env.VITE_BASE_SOCKET_PATH + '/websocket/con';
if (import.meta.env.MODE === 'docker') {
	 wsuri ='ws://'+window.location.host+import.meta.env.VITE_BASE_SOCKET_PATH + '/websocket/con';
}
console.log(wsuri)
import { getToken } from '@/utils/auth'
import { store } from '../store';

function createWebSocket(func) {
	if (websock == null || websock.readyState !== 1) {
		initWebSocket(func);
	}
}
function initWebSocket(func) {
	// 初始化websocket
	console.log(wsuri)
	websock = new WebSocket(wsuri + '?token=' + getToken());
	websock.onmessage = function (e) {
		websocketonmessage(e);
	};
	websock.onclose = function (e) {
		websocketclose(e);
	};
	websock.onopen = function () {
		websocketOpen();
		func && func()
		heartCheck.reset().start();
	};

	// 连接发生错误的回调方法
	websock.onerror = function () {
		console.log("WebSocket连接发生错误");
		heartCheck.reset().resetSocket();
	};
}
// 实际调用的方法
function sendSock(agentData) {
	if (websock.readyState === websock.OPEN) {
		// 若是ws开启状态
		websocketsend(agentData);
	} else if (websock.readyState === websock.CONNECTING) {
		// 若是 正在开启状态，则等待1s后重新调用
		setTimeout(function () {
			sendSock(agentData);
		}, 1000);
	} else {
		heartCheck.reset().resetSocket(() => { sendSock(agentData) })
	}
}

function closeSock() {
	websock.close();
}
// 数据接收
function websocketonmessage({ data }) {
	console.log("websocket收到", data);
	if (data !== 'pong')
		try {
			let obj = JSON.parse(data)
			if (obj.event)
				store.dispatch('socket/' + obj.event, obj)
		} catch (error) {
			console.log(error)
		}
}

// 数据发送
function websocketsend(agentData) {
	// console.log("发送数据：" + agentData);
	websock.send(agentData);
}

// 关闭
function websocketclose(e) {
	console.log("断开连接：" + e);
	heartCheck.reset()
	if (e !== 'logout') {
		heartCheck.resetSocket();
	}
}

function websocketOpen(e) {
	console.log("连接打开");
}

//心跳检测
var heartCheck = {
	heartTimeout: 20000,        //20秒心跳
	resetTimeout: 10000,        //5秒钟reset
	timeoutObj: null,
	resetTimeoutObj: null,
	reset: function () {
		clearTimeout(this.timeoutObj);
		clearTimeout(this.resetTimeoutObj);
		return this;
	},
	start: function () {
		var self = this;
		this.timeoutObj = setTimeout(function () {
			sendSock("ping");
			self.reset().start();
		}, this.heartTimeout)
	},
	resetSocket: function (func) {
		var self = this;
		this.resetTimeoutObj = setTimeout(function () {
			createWebSocket(func)
			self.reset().resetSocket();
		}, this.resetTimeout)
	},
}
export default function useSocket() {
	return {
		createWebSocket,
		sendSock,
		websocketclose
	};
}