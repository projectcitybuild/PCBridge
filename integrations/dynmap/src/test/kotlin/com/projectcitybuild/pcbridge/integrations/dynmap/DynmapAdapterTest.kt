package com.projectcitybuild.pcbridge.integrations.dynmap

import org.dynmap.DynmapCommonAPI
import org.dynmap.markers.AreaMarker
import org.dynmap.markers.CircleMarker
import org.dynmap.markers.MarkerAPI
import org.dynmap.markers.MarkerSet
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class DynmapAdapterTest {
    @Test
    fun `removes both possible marker shapes for an unloaded world`() {
        val delegate = mock(DynmapDelegate::class.java)
        val dynmap = mock(DynmapCommonAPI::class.java)
        val markerAPI = mock(MarkerAPI::class.java)
        val markerSet = mock(MarkerSet::class.java)
        val areaMarker = mock(AreaMarker::class.java)
        val circleMarker = mock(CircleMarker::class.java)
        val worldId = UUID.randomUUID()
        val markerId = "world_border_$worldId"
        whenever(delegate.instance).thenReturn(dynmap)
        whenever(dynmap.markerAPI).thenReturn(markerAPI)
        whenever(markerAPI.getMarkerSet("pcbridge_world_borders")).thenReturn(markerSet)
        whenever(markerSet.findAreaMarker(markerId)).thenReturn(areaMarker)
        whenever(markerSet.findCircleMarker(markerId)).thenReturn(circleMarker)
        val adapter = DynmapAdapter(delegate)

        adapter.removeBorderMarker(worldId)

        verify(areaMarker).deleteMarker()
        verify(circleMarker).deleteMarker()
    }

    @Test
    fun `ignores an unloaded world when the Dynmap border marker set is absent`() {
        val delegate = mock(DynmapDelegate::class.java)
        val dynmap = mock(DynmapCommonAPI::class.java)
        val markerAPI = mock(MarkerAPI::class.java)
        whenever(delegate.instance).thenReturn(dynmap)
        whenever(dynmap.markerAPI).thenReturn(markerAPI)
        whenever(markerAPI.getMarkerSet("pcbridge_world_borders")).thenReturn(null)

        DynmapAdapter(delegate).removeBorderMarker(UUID.randomUUID())
    }
}
