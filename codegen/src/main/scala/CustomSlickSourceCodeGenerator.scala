import slick.codegen.SourceCodeGenerator
import slick.model.Model

class CustomSlickSourceCodeGenerator(model: Model) extends SourceCodeGenerator(model) {

  override def Table = new Table(_) {
    override def TableClass: AnyRef with TableClassDef = new TableClass() {
      override def code: String = {
        val prns = parents.map(" with " + _).mkString("")
        val args = Seq("\"" + model.name.table + "\"")
        s"""
class $name(_tableTag: Tag) extends profile.api.Table[$elementType](_tableTag, ${args.mkString(", ")})$prns {
  ${indent(body.map(_.mkString("\n")).mkString("\n\n"))}
}
        """.trim()
      }
    }

    override def Column = new Column(_) {

      override def rawType: String = model.tpe match {
        case "java.sql.Timestamp" => "LocalDateTime"
        case "java.sql.Date"      => "LocalDate"
        case "java.sql.Time"      => "LocalTime"
        case _                    => super.rawType
      }

      override def default: Option[String] = rawName match {
        case "createdAt" | "updatedAt" => Some("LocalDateTime.now")
        case _                         => super.default
      }
    }
  }

  override def codePerTable: Map[String, String] = {
    tables
      .map(table => {
        val before =
          "import slick.model.ForeignKeyAction\n" + "import java.time._\n" +
            (if (table.hlistEnabled || table.isMappedToHugeClass) {
               "import slick.collection.heterogeneous._\n" +
                 "import slick.collection.heterogeneous.syntax._\n"
             } else "") +
            (if (table.PlainSqlMapper.enabled) {
               "// NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.\n" +
                 "import slick.jdbc.{GetResult => GR}\n"
             } else "")

        (table.TableValue.name, table.code.mkString(before, "\n", ""))
      })
      .toMap
  }
}
