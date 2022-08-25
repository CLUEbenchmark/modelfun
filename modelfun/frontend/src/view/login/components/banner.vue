<template>
    <div class="banner">
        <div class="banner-inner">
            <a-carousel class="carousel" animation-name="fade">
                <a-carousel-item v-for="item in carouselItem" :key="item.slogan">
                    <div :key="item.slogan" class="carousel-item">
                        <!-- <div class="carousel-title">{{ item.slogan }}</div> -->
                        <div class="carousel-sub-title1">{{ item.slogan }} {{ item.subSlogan1 }} {{ item.subSlogan2 }}</div>
                        <!-- <div class="carousel-sub-title2">{{ item.subSlogan2 }}</div> -->
                        <!-- <img class="carousel-image" :src="item.image" /> -->
                        <!-- <div class="carousel-sub-title1">{{ item.slogan }}
                            <img src="@/assets/AI.png" alt="">
                            {{ item.subSlogan2 }}
                        </div> -->
                    </div>
                </a-carousel-item>
            </a-carousel>
        </div>
    </div>
</template>

<script  setup>
import { computed } from "vue";
import bannerImage from "@/assets/logo.png";
const carouselItem = computed(() => [
    {
        slogan: "Unlock",
        subSlogan1: "AI",
        subSlogan2: "Potential",
        image: bannerImage,
    },
]);
</script>

<style lang="less" scoped>
.banner {
    display: flex;
    align-items: center;
    justify-content: center;

    &-inner {
        flex: 1;
        height: 100%;
    }
}

.carousel {
    height: 100%;

    &-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100%;
    }

    &-title {
        color: var(--color-fill-1);
        font-weight: 500;
        font-size: 40px;
        line-height: 28px;
        align-self: flex-start;
        margin-left: 20px;
        margin-bottom: 40px;
        // background: linear-gradient(to bottom, blue, red);
        // -webkit-background-clip: text;
        // color: transparent;
    }

    &-sub-title1,
    &-sub-title2 {
        margin-top: 8px;
        color: var(--color-fill-1);
        font-size: 28px;
        line-height: 22px;
        align-self: flex-end;
        margin-right: 20px;
    }
    &-sub-title1 {
        align-self: center;
        font-size: 32px;
        margin: 30px 0;
        white-space: nowrap;
        display: flex;
        align-items: center;
        img{
          width: 180px;
          height: 180px;
          // margin-left: 10px;
        }
    }

    &-image {
        width: 320px;
        margin-top: 30px;
    }
}
</style>
