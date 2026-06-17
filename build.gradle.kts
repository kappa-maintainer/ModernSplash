
plugins {
    id("com.gtnewhorizons.gtnhconvention")
    java
}

java {
    toolchain {
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}
