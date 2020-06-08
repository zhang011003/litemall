<template>
    <div class="login">
        <div class="store_header">
            <van-icon name="fail" color="green" size="80"/>
            <div class="store_name">{{ errorInfo }}</div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'error',
    props: {
        errorType: [String, Number]
    },

    created() {
        this.initData();
    },

    data() {
        return {
            errorInfos: ["url地址错误"],
            errorInfo: '',

        };
    },
    methods: {
        initData() {
            this.errorInfo = this.errorInfos[Number(this.errorType)]
        }
    }

}
</script>

<style lang="scss" scoped>
.login {
    position: relative;
    background-color: #fff;
}
.store_header {
    text-align: center;
    padding: 30px 0;
    .store_avatar img {
        border-radius: 50%;
    }
    .store_name {
        padding-top: 5px;
        font-size: 16px;
    }
}
</style>