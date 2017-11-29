Dim oWMI : Set oWMI = GetObject("winmgmts:")
Dim classComponent : Set classComponent = oWMI.ExecQuery("SELECT DeviceID FROM Win32_PnPEntity WHERE DeviceID LIKE 'PCI%'")
Dim obj
For Each obj in classComponent
  wscript.echo obj.DeviceID
Next
