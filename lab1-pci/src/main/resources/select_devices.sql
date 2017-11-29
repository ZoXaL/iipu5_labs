--select
----  both device_id and vendor_id are present
----    devices.device_id as DID, devices.device_name as DN, devices.device_description as DD,
----    vendors.vendor_id as VID, vendors.vendor_name_short as VNS, vendors.vendor_name_full as VN
--    vendors.vendor_id as VID, devices.device_id as DN
--    from
--        devices inner join
--        (values ('1022', '1705'), ('1002', '9648')) as V(device_id, vendor_id)
--        on (devices.device_id = V.device_id and devices.vendor_id = V.vendor_id)
--        inner join vendors on (devices.vendor_id = vendors.vendor_id)
--union
--select
----  only device_id or vendor_id are present
--   orphan_devices.device_name as DN, vendors.vendor_name_short as DD
----    orphan_devices.device_id as DID, orphan_devices.device_name as DN, orphan_devices.device_description as DD,
----    orphan_devices.vendor_id as VID, vendors.vendor_name_short as VNS, vendors.vendor_name_full as VN
----        orphan_devices.vendor_id as VID, orphan_devices.device_id as DN
--    from
--        vendors right outer join (
--      fill info by deviceId
        select
            V.device_id as device_id,
            V.vendor_id as vendor_id,
            devices.device_name as device_name,
            devices.device_description as device_description
            from
                DEVICES right outer join
                (values ('1002', '9648')) as V(device_id, vendor_id)
                on (devices.device_id = V.device_id and devices.vendor_id = V.vendor_id)
--        ) as orphan_devices
--        on (orphan_devices.vendor_id = vendors.vendor_id)