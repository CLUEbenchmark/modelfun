let obj = {
    range: {
        '&0': function (len, start, end) {
            return `${start}.{${len}}${end}`
        },
        '&1': function (len, start, end) {
            return `${end}.*${start}.*${end}`
        },
        '&2': function (len, start, end) {
            return `${start}.*${end}`
        },
    },
    len: {
        '&0': function (len, start, end) {
            return `^.{${len}}$`
        },
        '&1': function (len, start, end) {
            return `^(.{${len - 1}})|(.{${len + 1}})$`
        },
        '&2': function (len, start, end) {
            return `^(.{${len},})$`
        },
        '&3': function (len, start, end) {
            return `^(.{0,${len}})$`
        },
        '&4': function (len, start, end) {
            return `^(.{${len + 1},})$`
        },
        '&5': function (len, start, end) {
            return `^(.{0,${len + 1}})$`
        },

    }
}
export const keyWord = [
    "False",
    "await",
    "else",
    "import",
    "pass",
    "None",
    "break",
    "except",
    "in",
    "raise",
    "True",
    "class",
    "finally",
    "is",
    "return",
    "and",
    "continue",
    "for",
    "lambda",
    "try",
    "as",
    "def",
    "from",
    "nonlocal",
    "while",
    "assert",
    "del",
    "global",
    "not",
    "with",
    "async",
    "elif",
    "if",
    "or",
    "yield",
];
export const sysFunc = [
    "abs",
    "delattr",
    "hash",
    "memoryview",
    "set",
    "all",
    "dict",
    "help",
    "min",
    "setattr",
    "any",
    "dir",
    "hex",
    "next",
    "slice",
    "ascii",
    "divmod",
    "id",
    "object",
    "sorted",
    "bin",
    "enumerate",
    "input",
    "oct",
    "staticmethod",
    "bool",
    "eval",
    "int",
    "open",
    "str",
    "breakpoint",
    "exec",
    "isinstance",
    "ord",
    "sum",
    "bytearray",
    "filter",
    "issubclass",
    "pow",
    "super",
    "bytes",
    "float",
    "iter",
    "print",
    "tuple",
    "callable",
    "format",
    "len",
    "property",
    "type",
    "chr",
    "frozenset",
    "list",
    "range",
    "vars",
    "classmethod",
    "getattr",
    "locals",
    "repr",
    "zip",
    "compile",
    "globals",
    "map",
    "reversed",
    "__import__",
    "complex",
    "hasattr",
    "max",
    "round",
];
export default obj