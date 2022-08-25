<template>
    <div class="in-coder-panel">
        <textarea style="height: 100% !important;" ref="textarea" v-model="code"></textarea>
    </div>
</template>

<script>
import _CodeMirror from "codemirror/lib/codemirror";

import "codemirror/lib/codemirror.css";
import "codemirror/theme/ayu-dark.css";
// 主题样式
import {
    reactive,
    defineComponent,
    toRefs,
    getCurrentInstance,
    onMounted,
    onBeforeUnmount,
    nextTick,
    watch,
} from "vue";
import "codemirror/mode/python/python.js";

// 尝试获取全局实例
const CodeMirror = window.CodeMirror || _CodeMirror;
let coder = null; // 编辑器实例

export default defineComponent({
    name: "codeEditor",
    props: {
        value: {
            type: String,
            default: "",
        },
        heightSize: {
            type: Number,
            default: 300,
        },
        scene: {
            type: String,
            default: "look", // add: 新增； edit: 编辑； look: 查看
        },
        eventType: {
            type: String,
            default: "change", // 可用事件'change', 'blur'等等；具体参考codemirror文档
        },
        theme: {
            type: String,
            default: "ayu-dark", // 编辑器主题色
        },
    },
    setup(props, { emit }) {
        const { proxy } = getCurrentInstance();
        const data = reactive({
            code: props.value, // 内部真实的内容
            // 默认配置
            options: {
                mode: "python", // 不设置的话，默认使用第一个引用
                // 缩进格式
                tabSize: 2,
                // 主题，对应主题库 JS 需要提前引入
                theme: props.theme,
                // 显示行号
                lineNumbers: true,
                readOnly:
                    props.scene === "add" || props.scene === "edit"
                        ? false
                        : true, // true: 不可编辑  false: 可编辑 'nocursor' 失焦,不可编辑
            },
            // 初始化
            initialize: () => {
                console.log(proxy.$refs.textarea)
                // 初始化编辑器实例，传入需要被实例化的文本域对象和默认配置
                coder = CodeMirror.fromTextArea(
                    proxy.$refs.textarea,
                    data.options
                );
                const h = props.heightSize + "px";
                coder.setSize("auto", h);
                // 此处也可使用'change'事件，不过每次书写的过程中都会触发，为了提高性能，故默认使用'blur'
                coder.on(props.eventType, (coder) => {
                    emit("update:value", coder.getValue());
                });
            },
        });
        onMounted(() => {
            data.initialize();
        });
        onBeforeUnmount(() => {
            coder.off(props.eventType);
        });
        return {
            ...toRefs(data),
        };
    },
});
</script>
<style>
.in-coder-panel {
    flex: 1;
    display: flex;
    position: relative;
    overflow: auto;
}

.in-coder-panel .CodeMirror {
    flex-grow: 1;
    text-align: left !important;
    z-index: 1;
}

.in-coder-panel .CodeMirror .CodeMirror-code {
    line-height: 20px;
}
.CodeMirror-scroll {
    /* width: 100px; */
}
.CodeMirror.cm-s-ayu-dark {
    width: 0px !important;
}
</style>