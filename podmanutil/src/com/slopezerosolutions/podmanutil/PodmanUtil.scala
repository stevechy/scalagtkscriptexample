package com.slopezerosolutions.podmanutil

import org.gnome.gtk.Align
import org.gnome.gtk.Application
import org.gnome.gtk.ApplicationWindow
import org.gnome.gtk.Box
import org.gnome.gtk.Button
import org.gnome.gtk.ColumnView
import org.gnome.gtk.ColumnViewColumn
import org.gnome.gtk.Gtk
import org.gnome.gtk.Label
import org.gnome.gtk.ListItem
import org.gnome.gtk.Orientation
import org.gnome.gtk.Paned
import org.gnome.gtk.SignalListItemFactory
import org.gnome.gtk.SingleSelection
import org.javagi.gio.ListIndexModel
import org.javagi.gio.ListIndexModel.ListIndex

object PodmanUtil {



  def main(args: Array[String]): Unit = {
    val app = new Application("my.example.PodmanUtil")
    app.onActivate(() => activate(app))
    app.run(args)
  }

  def activate(app: Application): Unit = {
    var containers = Seq[PodmanContainer]()
    val listIndexModel = new ListIndexModel(containers.size)
    val window = new ApplicationWindow(app)

    val selection = new SingleSelection(listIndexModel)
    val list = new ColumnView(selection)
    val column: ColumnViewColumn = columnView(containers, "Name", _.name)
    column.setExpand(true)
    list.appendColumn(column)
    val statusColumn: ColumnViewColumn = columnView(containers, "Status", _.status)
    statusColumn.setExpand(true)
    list.appendColumn(statusColumn)
    val portsColumn: ColumnViewColumn = columnView(containers, "Ports", _.ports)
    portsColumn.setExpand(true)
    list.appendColumn(portsColumn)
    list.setHalign(Align.FILL)
    list.setHexpand(true)

    def refresh(): Unit = {
      val result = os.call(("podman", "ps", "-a", "--format", """{ "name": "{{.Names}}", "status": "{{.Status}}", "ports": "{{.Ports}}" }"""))
      val resultStr = result.out.text()
      containers = resultStr.linesIterator.map(line => upickle.read[PodmanContainer](line)).toSeq
      listIndexModel.setSize(containers.size)
    }

    val box =  new Box(Orientation.VERTICAL, 8){
      setHalign(Align.CENTER)
      setValign(Align.CENTER)
    }

    val startButton = Button.withLabel("Start")
    startButton.onClicked(() => {
      val selected = selection.getSelected
      if (selected != Gtk.INVALID_LIST_POSITION) {
        val container = containers(selected)
        os.call(("podman", "start", container.name))
        refresh()
      }
    })
    box.append(startButton)

    val stopButton = Button.withLabel("Stop")
    stopButton.onClicked(() => {
      val selected = selection.getSelected
      if (selected != Gtk.INVALID_LIST_POSITION) {
        val container = containers(selected)
        os.call(("podman", "stop", container.name))
        refresh()
      }
    })
    box.append(stopButton)

    val button = Button.withLabel("Refresh")
    button.onClicked(() => {
      refresh()
    })
    box.append(button)

    val buttonClose = Button.withLabel("Close")
    buttonClose.onClicked(() => window.close())
    box.append(buttonClose)

    val pane = new Paned(Orientation.HORIZONTAL)
    pane.setStartChild(list)
    pane.setEndChild(box)

    window.setDefaultSize(600, 500)
    window.setChild(pane)
    window.setTitle("Podman util")
    window.present()
  }

  private def columnView(containers: => Seq[PodmanContainer], columnName: String, formatValue: PodmanContainer => String) = {
    val factory = new SignalListItemFactory()
    factory.onSetup(item => {
      val listItem = item.asInstanceOf[ListItem]
      listItem.setChild(new Label(""))
    })
    factory.onBind(item => {
      val listItem = item.asInstanceOf[ListItem]
      (listItem.getChild, listItem.getItem) match {
        case (label: Label, listIndex: ListIndex) => {
          label.setLabel(formatValue(containers(listIndex.getIndex)))
        }
        case _ => {}
      }
    })
    val column = new ColumnViewColumn(columnName, factory)
    column
  }
}
