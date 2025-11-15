-- Script để test và debug threshold update
-- Chạy script này trong SQL Server Management Studio để kiểm tra

-- 1. Kiểm tra stored procedure có tồn tại không
SELECT 
    name AS ProcedureName,
    type_desc AS ProcedureType,
    create_date,
    modify_date
FROM sys.procedures
WHERE name = 'UpdateThreshold';

-- 2. Kiểm tra bảng Threshold có dữ liệu không
SELECT TOP 10
    ThresholdID,
    SensorTypeID,
    LevelName,
    MinValue,
    MaxValue,
    AlertLevel,
    Message
FROM Threshold
ORDER BY SensorTypeID, MinValue;

-- 3. Kiểm tra bảng User có user với ID = 1 không (cho UpdatedBy)
SELECT UserID, Username, FullName, IsActive
FROM [User]
WHERE UserID = 1;

-- 4. Test stored procedure với một threshold ID cụ thể
-- Thay ThresholdID và UpdatedBy bằng giá trị thực tế từ database của bạn
DECLARE @TestThresholdID INT = 1; -- Thay bằng ID thực tế
DECLARE @TestUpdatedBy INT = 1;   -- Thay bằng UserID thực tế

-- Kiểm tra threshold có tồn tại không
IF EXISTS (SELECT 1 FROM Threshold WHERE ThresholdID = @TestThresholdID)
BEGIN
    PRINT 'Threshold ID ' + CAST(@TestThresholdID AS VARCHAR) + ' exists';
    
    -- Test stored procedure
    BEGIN TRY
        EXEC UpdateThreshold 
            @ThresholdID = @TestThresholdID,
            @LevelName = N'Test Level',
            @MinValue = 0.0,
            @MaxValue = 100.0,
            @AlertLevel = 0,
            @Message = N'Test message',
            @UpdatedBy = @TestUpdatedBy;
        
        PRINT 'Stored procedure executed successfully';
    END TRY
    BEGIN CATCH
        PRINT 'Error executing stored procedure:';
        PRINT ERROR_MESSAGE();
        PRINT 'Error Number: ' + CAST(ERROR_NUMBER() AS VARCHAR);
        PRINT 'Error State: ' + CAST(ERROR_STATE() AS VARCHAR);
    END CATCH
END
ELSE
BEGIN
    PRINT 'Threshold ID ' + CAST(@TestThresholdID AS VARCHAR) + ' does not exist';
END

-- 5. Kiểm tra log table
SELECT TOP 10
    LogID,
    ThresholdID,
    UpdatedBy,
    OldMinValue,
    OldMaxValue,
    NewMinValue,
    NewMaxValue,
    UpdatedAt
FROM ThresholdUpdateLog
ORDER BY UpdatedAt DESC;

