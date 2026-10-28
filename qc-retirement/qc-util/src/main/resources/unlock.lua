-- 锁的key
local key = KEYS[1]
-- 锁的value
local value = ARGV[1]

-- 获取锁的线程标识
local id = redis.call('get',key)
-- 比较标识
if(value == id) then
    --释放锁
    return redis.call('del',key)
end
return 0