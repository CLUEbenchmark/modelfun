//保留小数
export const toFixed = (num, fixed = 2) => {
    if (Number(num)) {
        return Number(num)?.toFixed(fixed)
    }else{
        return '0.00'
    }
} 
