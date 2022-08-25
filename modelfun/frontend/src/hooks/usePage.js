import { reactive } from 'vue';
export default function usePage(func, {
    current = 1,
    total = 0,
    pageSize = 10,
    showTotal = true,
    showJumper = true,
    showPageSize=true,
} = {}) {
    const pagination = reactive({
        current: current,
        total: total,
        pageSize: pageSize,
        showTotal: showTotal,
        showJumper: showJumper,
        showPageSize:showPageSize,
    })
    const pageChange = (page) => {
        pagination.current = page
        func && func()
    }
    const pageSizeChange = (pageSize) => {
        pagination.pageSize = pageSize
        func && func()
    }
    return {
        pagination,
        pageChange,
        pageSizeChange
    };
}
